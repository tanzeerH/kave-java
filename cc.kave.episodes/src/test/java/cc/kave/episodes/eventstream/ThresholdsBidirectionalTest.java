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
package cc.kave.episodes.eventstream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cc.kave.episodes.io.EpisodeParser;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.statistics.EpisodesStatistics;
import cc.recommenders.exceptions.AssertionException;
import cc.recommenders.io.Logger;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ThresholdsBidirectionalTest {

	@Rule
	public TemporaryFolder rootFolder = new TemporaryFolder();
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private EpisodeParser parser;

	private EpisodesStatistics stats;
	
	private static final int NUMBREPOS = 2;
	private static final int FREQTHRESH = 3;
	
	private ThresholdsBidirection sut;
	
	@Before
	public void setup() throws IOException {
		Logger.reset();
		Logger.setCapturing(true);

		MockitoAnnotations.initMocks(this);
		
		Map<Integer, Set<Episode>> episodes = Maps.newLinkedHashMap();
		Set<Episode> currEpLevel = Sets.newLinkedHashSet();
		
		currEpLevel.add(createEpisode(3, 0.7, "1"));
		currEpLevel.add(createEpisode(2, 0.5, "2"));
		episodes.put(1, currEpLevel);
		
		currEpLevel = Sets.newLinkedHashSet();
		currEpLevel.add(createEpisode(3, 0.6, "1", "2", "1>2"));
		currEpLevel.add(createEpisode(2, 0.6, "1", "3", "1>3"));
		currEpLevel.add(createEpisode(3, 0.8, "2", "3", "2>3"));
		episodes.put(2, currEpLevel);
		
		currEpLevel = Sets.newLinkedHashSet();
		currEpLevel.add(createEpisode(2, 0.9, "1", "2", "3", "1>2", "1>3"));
		currEpLevel.add(createEpisode(3, 1.0, "1", "3", "4", "1>3", "1>4"));
		episodes.put(3, currEpLevel);
		
		stats = new EpisodesStatistics();
		
		sut = new ThresholdsBidirection(rootFolder.getRoot(), parser, stats);
		
		when(parser.parse(any(File.class))).thenReturn(episodes);
	}
	
	@After
	public void teardown() {
		Logger.reset();
	}
	
	@Test
	public void cannotBeInitializedWithNonExistingFolder() {
		thrown.expect(AssertionException.class);
		thrown.expectMessage("Patterns folder does not exist");
		sut = new ThresholdsBidirection(new File("does not exist"), parser, stats);
	}

	@Test
	public void cannotBeInitializedWithFile() throws IOException {
		File file = rootFolder.newFile("a");
		thrown.expect(AssertionException.class);
		thrown.expectMessage("Patterns is not a folder, but a file");
		sut = new ThresholdsBidirection(file, parser, stats);
	}

	@Test
	public void mockIsCalled() throws ZipException, IOException {
		sut.writer(NUMBREPOS, FREQTHRESH);

		verify(parser).parse(any(File.class));
	}
	
	@Test
	public void filesAreCreated() throws IOException {
		sut.writer(NUMBREPOS, FREQTHRESH);

		verify(parser).parse(any(File.class));

		File bdsFile = new File(getBdsPath());

		assertTrue(bdsFile.exists());
	}
	
	@Test
	public void contentTest() throws IOException {
		sut.writer(NUMBREPOS, FREQTHRESH);

		verify(parser).parse(any(File.class));

		File bdsFile = new File(getBdsPath());
		
		StringBuilder expBds = new StringBuilder();
		expBds.append("Bidirectional distribution for 2-node episodes:\n");
		expBds.append("Bidirectional\tCounter\n");
		expBds.append("0.6\t2\n");
		expBds.append("0.8\t1\n\n");
		
		expBds.append("Bidirectional distribution for 3-node episodes:\n");
		expBds.append("Bidirectional\tCounter\n");
		expBds.append("1.0\t1\n\n");
		
		String actualBds = FileUtils.readFileToString(bdsFile);
		
		assertEquals(expBds.toString(), actualBds);
	}

	private String getBdsPath() {
		File streamFile = new File(rootFolder.getRoot().getAbsolutePath() + "/bds" + FREQTHRESH + "Freq" + NUMBREPOS + "Repos.txt");
		return streamFile.getAbsolutePath();
	}
	
	private Episode createEpisode(int freq, double bdmeas, String... strings) {
		Episode episode = new Episode();
		episode.setFrequency(freq);
		episode.setEntropy(bdmeas);
		for (String fact : strings) {
			episode.addFact(fact);
		}
		return episode;
	} 
}
