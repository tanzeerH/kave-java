/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.kave.episodes.mining.evaluation;

import static cc.recommenders.testutils.LoggerUtils.assertLogContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cc.kave.commons.model.episodes.Event;
import cc.kave.commons.model.episodes.Events;
import cc.kave.commons.model.episodes.Fact;
import cc.kave.commons.model.names.IMethodName;
import cc.kave.commons.model.names.csharp.MethodName;
import cc.kave.episodes.mining.patterns.MaximalEpisodes;
import cc.kave.episodes.mining.reader.MappingParser;
import cc.kave.episodes.mining.reader.ReposParser;
import cc.kave.episodes.mining.reader.StreamParser;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.postprocessor.EpisodesPostprocessor;
import cc.recommenders.exceptions.AssertionException;
import cc.recommenders.io.Logger;

public class PatternsIdentifierTest {

	@Rule
	public TemporaryFolder rootFolder = new TemporaryFolder();
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static final int NUMBREPOS = 2;
	private static final int FREQUENCY = 2;
	private static final double ENTROPY = 0.5;

	@Mock
	private StreamParser streamParser;
	@Mock
	private MappingParser mappingParser;
	@Mock
	private EpisodesPostprocessor processor;
	@Mock
	private MaximalEpisodes maxEpisodes;
	@Mock
	private ReposParser repos;

	Map<Integer, Set<Episode>> patterns;
	List<List<Fact>> stream;
	List<Event> events;
	List<Event> validationStream;

	private PatternsIdentifier sut;

	@Before
	public void setup() throws IOException {
		Logger.reset();
		Logger.setCapturing(true);

		MockitoAnnotations.initMocks(this);

		patterns = Maps.newLinkedHashMap();
		Set<Episode> episodes = Sets.newLinkedHashSet();
		Episode ep = new Episode();
		ep.addFact(new Fact(1));
		episodes.add(ep);

		ep = new Episode();
		ep.addFact(new Fact(2));
		episodes.add(ep);
		patterns.put(1, episodes);

		stream = new LinkedList<>();
		List<Fact> method = Lists.newArrayList(new Fact(1), new Fact(2), new Fact(3), new Fact(4));
		stream.add(method);
		method = Lists.newArrayList(new Fact(5), new Fact(6), new Fact(7), new Fact(3));
		stream.add(method);
		method = Lists.newArrayList(new Fact(8), new Fact(4), new Fact(3));
		stream.add(method);
		method = Lists.newArrayList(new Fact(5), new Fact(9), new Fact(3));
		stream.add(method);

		events = Lists.newArrayList(dummy(), firstCtx(1), enclosingCtx(2), inv(3), inv(4), firstCtx(5), superCtx(6),
				enclosingCtx(7), enclosingCtx(8), enclosingCtx(9));

		validationStream = Lists.newArrayList(firstCtx(1), enclosingCtx(2), inv(3), inv(4), firstCtx(5), superCtx(6),
				enclosingCtx(7), inv(3), firstCtx(0), enclosingCtx(8), inv(4), inv(3), firstCtx(5), enclosingCtx(9),
				inv(3));

		sut = new PatternsIdentifier(rootFolder.getRoot(), streamParser, processor, mappingParser, maxEpisodes, repos);

		when(streamParser.parseStream(anyInt())).thenReturn(stream);
		when(mappingParser.parse(anyInt())).thenReturn(events);
		when(processor.postprocess(anyInt(), anyInt(), anyDouble())).thenReturn(patterns);
		when(maxEpisodes.getMaximalEpisodes(any(Map.class))).thenReturn(patterns);
		when(repos.validationStream(anyInt())).thenReturn(validationStream);
	}

	@After
	public void teardown() {
		Logger.reset();
	}

	@Test
	public void cannotBeInitializedWithNonExistingFolder() {
		thrown.expect(AssertionException.class);
		thrown.expectMessage("Patterns folder does not exist");
		sut = new PatternsIdentifier(new File("does not exist"), streamParser, processor, mappingParser, maxEpisodes,
				repos);
	}

	@Test
	public void cannotBeInitializedWithFile() throws IOException {
		File file = rootFolder.newFile("a");
		thrown.expect(AssertionException.class);
		thrown.expectMessage("Patterns folder is not a folder, but a file");
		sut = new PatternsIdentifier(file, streamParser, processor, mappingParser, maxEpisodes, repos);
	}

	@Test
	public void mocksAreCalledInTraining() throws Exception {
		sut.trainingCode(NUMBREPOS, FREQUENCY, ENTROPY);

		verify(streamParser).parseStream(anyInt());
		verify(mappingParser).parse(anyInt());
		verify(processor).postprocess(anyInt(), anyInt(), anyDouble());
		verify(maxEpisodes).getMaximalEpisodes(any(Map.class));
	}
	
	@Test
	public void mocksAreCalledInValidation() throws Exception {
		sut.validationCode(NUMBREPOS, FREQUENCY, ENTROPY);

		verify(mappingParser).parse(anyInt());
		verify(processor).postprocess(anyInt(), anyInt(), anyDouble());
		verify(repos).validationStream(anyInt());
	}

	@Test
	public void excepsionIsCalled() throws Exception {
		Episode ep = new Episode();
		ep.addStringsOfFacts("3", "4", "3>4");
		ep.setFrequency(3);
		patterns.put(2, Sets.newHashSet(ep));

		thrown.expect(Exception.class);
		thrown.expectMessage("Episode is not found sufficient number of times on the training stream!");
		sut.trainingCode(NUMBREPOS, FREQUENCY, ENTROPY);
	}

	@Test
	public void loggerIsCalledInTraining() throws Exception {
		Set<Episode> episodes = Sets.newLinkedHashSet();
		Episode ep = new Episode();
		ep.addStringsOfFacts("3", "4");
		ep.setFrequency(2);
		episodes.add(ep);

		ep = new Episode();
		ep.addStringsOfFacts("5", "3", "5>3");
		ep.setFrequency(2);
		episodes.add(ep);
		patterns.put(2, episodes);

		sut.trainingCode(NUMBREPOS, FREQUENCY, ENTROPY);

		assertLogContains(0, "Processed 2-node patterns!");
	}
	
	@Test
	public void loggerIsCalledInValidation() throws Exception {
		Set<Episode> episodes = Sets.newLinkedHashSet();
		Episode ep = new Episode();
		ep.addStringsOfFacts("3", "4");
		ep.setFrequency(2);
		episodes.add(ep);

		ep = new Episode();
		ep.addStringsOfFacts("5", "3", "5>3");
		ep.setFrequency(2);
		episodes.add(ep);
		patterns.put(2, episodes);

		sut.validationCode(NUMBREPOS, FREQUENCY, ENTROPY);

		assertLogContains(0, "Processed 2-node patterns!");
	}
	
	@Test
	public void fileIsCreated() throws Exception {
		File fileName = getFilePath(NUMBREPOS, FREQUENCY, ENTROPY);
		
		sut.validationCode(NUMBREPOS, FREQUENCY, ENTROPY);
		
		assertTrue(fileName.exists());
	}
	
	@Test
	public void checkFileContent() throws Exception {
		Set<Episode> episodes = Sets.newLinkedHashSet();
		Episode ep = new Episode();
		ep.addStringsOfFacts("3", "4", "3>4");
		ep.setFrequency(2);
		episodes.add(ep);

		ep = new Episode();
		ep.addStringsOfFacts("5", "3", "5>3");
		ep.setFrequency(2);
		episodes.add(ep);
		patterns.put(2, episodes);
		
		sut.validationCode(NUMBREPOS, FREQUENCY, ENTROPY);
		
		String actuals = FileUtils.readFileToString(getFilePath(NUMBREPOS, FREQUENCY, ENTROPY));
		
		StringBuilder sb = new StringBuilder();
		sb.append("Patterns of size: 2-events\n");
		sb.append("PatternID\tFrequency\toccurrencesAsSet\toccurrencesOrder\n");
		sb.append("0\t2\t2\t1\n");
		sb.append("1\t2\t2\t2\n\n");
		
		assertEquals(sb.toString(), actuals);
	}

	private static Event inv(int i) {
		return Events.newInvocation(m(i));
	}

	private static Event firstCtx(int i) {
		return Events.newFirstContext(m(i));
	}

	private static Event superCtx(int i) {
		return Events.newSuperContext(m(i));
	}

	private static Event enclosingCtx(int i) {
		return Events.newContext(m(i));
	}

	private static Event dummy() {
		return Events.newDummyEvent();
	}

	private static IMethodName m(int i) {
		if (i == 0) {
			return MethodName.UNKNOWN_NAME;
		} else {
			return MethodName.newMethodName("[T,P] [T,P].m" + i + "()");
		}
	}
	
	private File getFilePath(int numbRepos, int freqThresh, double bidirectThresh) {
		File fileName = new File(rootFolder.getRoot().getAbsolutePath() + "/Repos" + numbRepos + "/Freq" + freqThresh
				+ "/Bidirect" + bidirectThresh + "/patternsValidation.txt");
		return fileName;
	}
}
