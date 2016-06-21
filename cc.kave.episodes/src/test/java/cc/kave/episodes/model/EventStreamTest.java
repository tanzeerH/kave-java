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
package cc.kave.episodes.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

import cc.kave.commons.model.episodes.Event;
import cc.kave.commons.model.episodes.Events;
import cc.kave.commons.model.names.IMethodName;
import cc.kave.commons.model.names.csharp.MethodName;

public class EventStreamTest {
	
	Map<Event, Integer> expectedMap;

	private EventStream sut;
	
	@Before
	public void setup() {
		expectedMap =  Maps.newLinkedHashMap();
		sut = new EventStream();
	}
	
	@Test
	public void defaultValues() {
		expectedMap.put(Events.newDummyEvent(), 0);
		
		assertEquals(expectedMap, sut.getMapping());
		assertEquals(1, sut.getEventNumber());
		assertEquals(0, sut.getStreamLength());
		
		assertTrue(sut.getStream().equals(""));
	}
	
	@Test
	public void addUnknownEvent() {
		sut.addEvent(Events.newUnknownEvent());
		
		expectedMap = Maps.newLinkedHashMap();
		expectedMap.put(Events.newDummyEvent(), 0);
		expectedMap.put(Events.newUnknownEvent(), 1);
		
		assertEquals(expectedMap, sut.getMapping());
		assertEquals(2, sut.getEventNumber());
		assertEquals(0, sut.getStreamLength());
		
		assertTrue(sut.getStream().equals(""));
	}
	
	@Test
	public void addContext() {
		sut.addEvent(ctx(1));
		
		expectedMap = Maps.newLinkedHashMap();
		expectedMap.put(Events.newDummyEvent(), 0);
		expectedMap.put(ctx(1), 1);
		
		String expectedStream = "1,0.500\n";
		String actualStream = sut.getStream();
		
		assertEquals(expectedMap, sut.getMapping());
		assertEquals(2, sut.getEventNumber());
		assertEquals(1, sut.getStreamLength());
		assertEquals(expectedStream, actualStream);
	}
	
	@Test
	public void addInvocation() {
		sut.addEvent(inv(1));
		
		expectedMap = Maps.newLinkedHashMap();
		expectedMap.put(Events.newDummyEvent(), 0);
		expectedMap.put(inv(1), 1);
		
		String expectedStream = "1,0.000\n";
		String actualStream = sut.getStream();
		
		assertEquals(expectedMap, sut.getMapping());
		assertEquals(2, sut.getEventNumber());
		assertEquals(1, sut.getStreamLength());
		assertEquals(expectedStream, actualStream);
	}
	
	@Test
	public void addMultipleEvents() {
		sut.addEvent(ctx(0));
		sut.addEvent(ctx(1));
		sut.addEvent(inv(2));
		sut.addEvent(inv(3));
		sut.addEvent(unknown());
		sut.addEvent(inv(2));
		
		Map<Event, Integer> expectedMap = Maps.newLinkedHashMap();
		expectedMap.put(Events.newDummyEvent(), 0);
		expectedMap.put(ctx(0), 1);
		expectedMap.put(ctx(1), 2);
		expectedMap.put(inv(2), 3);
		expectedMap.put(inv(3), 4);
		expectedMap.put(unknown(), 5);
		
		StringBuilder expectedSb = new StringBuilder();
		expectedSb.append("1,0.500\n");
		expectedSb.append("2,1.001\n");
		expectedSb.append("3,1.002\n");
		expectedSb.append("4,1.003\n");
		expectedSb.append("3,1.504\n");
		
		assertEquals(expectedMap, sut.getMapping());
		assertEquals(6, sut.getEventNumber());
		assertEquals(5, sut.getStreamLength());
		assertEquals(expectedSb.toString(), sut.getStream());
	}
	
	@Test
	public void equality_default() {
		EventStream a = new EventStream();
		EventStream b = new EventStream();
		
		assertTrue(a.equals(b));
	}
	
	@Test
	public void equlityReallySame() {
		EventStream a = new EventStream();
		a.addEvent(ctx(1));
		a.addEvent(inv(2));
		
		EventStream b = new EventStream();
		b.addEvent(ctx(1));
		b.addEvent(inv(2));
		
		assertEquals(a.getMapping(), b.getMapping());
		assertEquals(a.getEventNumber(), b.getEventNumber());
		assertEquals(a.getStreamLength(), b.getStreamLength());
		assertEquals(a.getStream(), b.getStream());		
		assertTrue(a.equals(b));
	}
	
	@Test
	public void notEqual1() {
		EventStream a = new EventStream();
		a.addEvent(ctx(1));
		a.addEvent(inv(2));
		
		EventStream b = new EventStream();
		b.addEvent(ctx(1));
		b.addEvent(inv(3));
		
		assertNotEquals(a.getMapping(), b.getMapping());
		assertEquals(a.getEventNumber(), b.getEventNumber());
		assertEquals(a.getStreamLength(), b.getStreamLength());
		assertEquals(a.getStream(), b.getStream());
		assertFalse(a.equals(b));
	}
	
	@Test
	public void notEqual2() {
		EventStream a = new EventStream();
		a.addEvent(ctx(1));
		a.addEvent(inv(2));
		
		EventStream b = new EventStream();
		b.addEvent(ctx(1));
		b.addEvent(inv(2));
		b.addEvent(inv(3));
		
		assertNotEquals(a.getMapping(), b.getMapping());
		assertNotEquals(a.getEventNumber(), b.getEventNumber());
		assertNotEquals(a.getStreamLength(), b.getStreamLength());
		assertNotEquals(a.getStream(), b.getStream());
		assertFalse(a.equals(b));
	}
	
	@Test
	public void notEqualStream() {
		EventStream a = new EventStream();
		a.addEvent(ctx(1));
		a.addEvent(inv(2));
		a.addEvent(inv(2));
		
		EventStream b = new EventStream();
		b.addEvent(ctx(1));
		b.addEvent(inv(2));
		
		assertEquals(a.getMapping(), b.getMapping());
		assertEquals(a.getEventNumber(), b.getEventNumber());
		assertNotEquals(a.getStreamLength(), b.getStreamLength());
		assertNotEquals(a.getStream(), b.getStream());
		assertFalse(a.equals(b));
	}
	
	private static Event inv(int i) {
		return Events.newInvocation(m(i));
	}

	private static Event ctx(int i) {
		return Events.newContext(m(i));
	}
	
	private static Event unknown() {
		return Events.newUnknownEvent();
	}
	
	private static IMethodName m(int i) {
		return MethodName.newMethodName("[T,P] [T,P].m" + i + "()");
	}
}
