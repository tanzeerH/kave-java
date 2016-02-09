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
package cc.kave.commons.model.ssts.impl.expressions.assignable;

import org.junit.Assert;
import org.junit.Test;

import cc.kave.commons.model.names.csharp.TypeName;
import cc.kave.commons.model.ssts.expressions.assignable.CastOperator;
import cc.kave.commons.model.ssts.impl.SSTBaseTest;
import cc.kave.commons.model.ssts.impl.SSTTestHelper;
import cc.kave.commons.model.ssts.impl.expressions.assignable.CastExpression;
import cc.kave.commons.model.ssts.impl.references.VariableReference;

public class CastExpressionTest extends SSTBaseTest {
	@Test
	public void testDefaultValues() {
		CastExpression sut = new CastExpression();
		Assert.assertEquals(new VariableReference(), sut.getReference());
		Assert.assertEquals(TypeName.UNKNOWN_NAME, sut.getTargetType());
		Assert.assertEquals(CastOperator.Unknown, sut.getOperator());
		Assert.assertNotEquals(0, sut.hashCode());
		Assert.assertNotEquals(1, sut.hashCode());
	}

	@Test

	public void testSettingValues() {
		CastExpression sut = new CastExpression();
		sut.setTargetType(TypeName.newTypeName("System.Int32, mscorlib, 4.0.0.0"));
		sut.setReference(someVarRef());
		sut.setOperator(CastOperator.SafeCast);

		Assert.assertEquals(TypeName.newTypeName("System.Int32, mscorlib, 4.0.0.0"), sut.getTargetType());
		Assert.assertEquals(someVarRef(), sut.getReference());
		Assert.assertEquals(CastOperator.SafeCast, sut.getOperator());
	}

	@Test

	public void testEquality_Default() {
		CastExpression a = new CastExpression();
		CastExpression b = new CastExpression();
		Assert.assertEquals(a, b);
		Assert.assertEquals(a.hashCode(), b.hashCode());
	}

	@Test

	public void testEquality_ReallyTheSame() {
		CastExpression a = new CastExpression();
		a.setTargetType(TypeName.newTypeName("System.Int32, mscorlib, 4.0.0.0"));
		a.setReference(someVarRef());
		a.setOperator(CastOperator.SafeCast);
		CastExpression b = new CastExpression();
		b.setTargetType(TypeName.newTypeName("System.Int32, mscorlib, 4.0.0.0"));
		b.setReference(someVarRef());
		b.setOperator(CastOperator.SafeCast);
		Assert.assertEquals(a, b);
		Assert.assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void testEquality_DifferentTargetType() {
		CastExpression a = new CastExpression();
		a.setTargetType(TypeName.newTypeName("System.Int32, mscorlib, 4.0.0.0"));
		CastExpression b = new CastExpression();

		Assert.assertNotEquals(a, b);
		Assert.assertNotEquals(a.hashCode(), b.hashCode());
	}

	@Test

	public void testEquality_DifferentOperator() {
		CastExpression a = new CastExpression();
		CastExpression b = new CastExpression();
		a.setOperator(CastOperator.SafeCast);
		Assert.assertNotEquals(a, b);
		Assert.assertNotEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void testEquality_DifferentVarRef() {
		CastExpression a = new CastExpression();
		a.setReference(someVarRef("i"));

		CastExpression b = new CastExpression();
		a.setReference(someVarRef("j"));

		Assert.assertNotEquals(a, b);
		Assert.assertNotEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void testVisitorIsImplemented() {
		CastExpression sut = new CastExpression();
		SSTTestHelper.accept(sut, 23).verify(sut);
	}

	@Test
	public void testNumberingOfEnumIsStable() {
		// IMPORTANT! do not change any of these because it will affect
		// serialization

		Assert.assertEquals(0, (int) CastOperator.Unknown.ordinal());
		Assert.assertEquals(1, (int) CastOperator.Cast.ordinal());
		Assert.assertEquals(2, (int) CastOperator.SafeCast.ordinal());
	}
}