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
package cc.kave.episodes.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.model.EventStream;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.Events;

public class EventsFilterTest {

	private static final int REMFREQS = 2;

	private List<Event> events;
	private List<Event> partitionEvents1;

	private EventStream expectedStream;
	private String expectedPartition1 = "";

	@Before
	public void setup() {
		events = Lists.newArrayList(firstCtx(1), enclosingCtx(6), inv(2), inv(3), firstCtx(0), superCtx(2),
				enclosingCtx(7), inv(5), inv(0), inv(2), firstCtx(1), enclosingCtx(6), inv(2), inv(3), firstCtx(3),
				superCtx(4), enclosingCtx(8), inv(3));
		// partitionEvents1 = Lists.newArrayList(ctx(2), inv(5), ctx(1), inv(4),
		// inv(3), inv(2));

		expectedStream = new EventStream();
		expectedStream.addEvent(firstCtx(1)); // 1
		expectedStream.addEvent(enclosingCtx(6)); // 2
		expectedStream.addEvent(inv(2)); // 3
		expectedStream.addEvent(inv(3)); // 4
		expectedStream.addEvent(firstCtx(0));
		expectedStream.addEvent(enclosingCtx(7)); // 5
		expectedStream.addEvent(inv(2)); // 3
		expectedStream.addEvent(firstCtx(3)); // 6
		expectedStream.addEvent(enclosingCtx(8)); // 7
		expectedStream.addEvent(inv(3)); // 4

		// expectedPartition1 +=
		// "1,0.000\n2,0.001\n3,0.002\n4,0.503\n2,0.504\n5,1.005\n6,1.006\n3,1.007\n";
	}

	@Test
	public void filterStream() {
		EventStream actuals = EventsFilter.filterStream(events, REMFREQS);

		assertEquals(expectedStream.getStream(), actuals.getStream());
		assertEquals(expectedStream.getMapping(), actuals.getMapping());
		assertEquals(expectedStream.getStreamLength(), actuals.getStreamLength());
		assertEquals(expectedStream.getNumberEvents(), actuals.getNumberEvents());
		assertTrue(expectedStream.equals(actuals));
	}

	@Ignore
	@Test
	public void filterPartition() {
		String actualsPartition = EventsFilter.filterPartition(partitionEvents1, expectedStream.getMapping());

		assertEquals(expectedPartition1, actualsPartition);
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

	private static IMethodName m(int i) {
		if (i == 0) {
			return Names.getUnknownMethod();
		} else {
			return Names.newMethod("[T,P, 1.2.3.4] [T,P, 1.2.3.4].m" + i + "()");
		}
	}
}