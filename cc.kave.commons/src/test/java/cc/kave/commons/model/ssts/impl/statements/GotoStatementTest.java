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
package cc.kave.commons.model.ssts.impl.statements;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import cc.kave.commons.model.ssts.impl.SSTTestHelper;
import cc.recommenders.testutils.ToStringAssert;

public class GotoStatementTest {

	@Test
	public void testDefaultValues() {
		GotoStatement sut = new GotoStatement();

		assertThat("", equalTo(sut.getLabel()));
		assertThat(0, not(equalTo(sut.hashCode())));
		assertThat(1, not(equalTo(sut.hashCode())));
	}

	@Test
	public void testSettingDefault() {
		GotoStatement sut = new GotoStatement();
		sut.setLabel("a");

		assertThat("a", equalTo(sut.getLabel()));
	}

	@Test
	public void testEqualityDefault() {
		GotoStatement a = new GotoStatement();
		GotoStatement b = new GotoStatement();

		assertThat(a, equalTo(b));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	public void testEqualityReallyTheSame() {
		GotoStatement a = new GotoStatement();
		GotoStatement b = new GotoStatement();
		a.setLabel("a");
		b.setLabel("a");

		assertThat(a, equalTo(b));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	public void testEqualityDifferentLabel() {
		GotoStatement a = new GotoStatement();
		GotoStatement b = new GotoStatement();
		a.setLabel("a");
		b.setLabel("b");

		assertThat(a, not(equalTo(b)));
		assertThat(a.hashCode(), not(equalTo(b.hashCode())));
	}

	@Test
	public void testVisitorIsImplemented() {
		GotoStatement sut = new GotoStatement();
		SSTTestHelper.accept(sut, 23).verify(sut);
	}

	@Test
	public void testWithReturnIsImplemented() {
		// TODO : Visitor Test
	}

	@Test
	public void toStringIsImplemented() {
		ToStringAssert.assertToStringUtils(new GotoStatement());
	}
}