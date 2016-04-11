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
import cc.kave.commons.model.ssts.expressions.assignable.ITypeCheckExpression;
import cc.kave.commons.model.ssts.impl.SSTBaseTest;
import cc.kave.commons.model.ssts.impl.SSTTestHelper;
import cc.kave.commons.model.ssts.impl.references.VariableReference;
import cc.kave.commons.utils.ToStringAssert;

public class TypeCheckExpressionTest extends SSTBaseTest {

	@Test
	public void testDefaultValues() {
		ITypeCheckExpression sut = new TypeCheckExpression();
		Assert.assertEquals(new VariableReference(), sut.getReference());
		Assert.assertEquals(TypeName.UNKNOWN_NAME, sut.getType());
		Assert.assertNotEquals(0, sut.hashCode());
		Assert.assertNotEquals(1, sut.hashCode());
	}

	@Test
	public void testSettingValues() {
		TypeCheckExpression sut = new TypeCheckExpression();
		sut.setReference(someVarRef());
		sut.setType(TypeName.newTypeName("System.Int32, mscorlib, 4.0.0.0"));
		Assert.assertEquals(TypeName.newTypeName("System.Int32, mscorlib, 4.0.0.0"), sut.getType());
		Assert.assertEquals(someVarRef(), sut.getReference());
	}

	@Test
	public void testEquality_Default() {
		TypeCheckExpression a = new TypeCheckExpression();
		TypeCheckExpression b = new TypeCheckExpression();
		Assert.assertEquals(a, b);
		Assert.assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void testEquality_ReallyTheSame() {
		TypeCheckExpression a = new TypeCheckExpression();
		a.setType(TypeName.newTypeName("System.Int32, mscorlib, 4.0.0.0"));
		a.setReference(someVarRef());

		TypeCheckExpression b = new TypeCheckExpression();
		b.setType(TypeName.newTypeName("System.Int32, mscorlib, 4.0.0.0"));
		b.setReference(someVarRef());

		Assert.assertEquals(a, b);
		Assert.assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void testEquality_DifferentType() {
		TypeCheckExpression a = new TypeCheckExpression();
		a.setType(TypeName.newTypeName("System.Int32, mscorlib, 4.0.0.0"));
		a.setReference(someVarRef());

		TypeCheckExpression b = new TypeCheckExpression();
		b.setType(TypeName.newTypeName("System.String, mscorlib, 4.0.0.0"));
		b.setReference(someVarRef());

		Assert.assertNotEquals(a, b);
		Assert.assertNotEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void testEquality_DifferentVarRef() {
		TypeCheckExpression a = new TypeCheckExpression();
		a.setType(TypeName.newTypeName("System.Int32, mscorlib, 4.0.0.0"));
		a.setReference(someVarRef("i"));

		TypeCheckExpression b = new TypeCheckExpression();
		b.setType(TypeName.newTypeName("System.Int32, mscorlib, 4.0.0.0"));
		b.setReference(someVarRef("j"));

		Assert.assertNotEquals(a, b);
		Assert.assertNotEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void testVisitorIsImplemented() {
		ITypeCheckExpression sut = new TypeCheckExpression();
		SSTTestHelper.accept(sut, 23).verify(sut);
	}

	@Test
	public void toStringIsImplemented() {
		ToStringAssert.AssertToStringUtils(new TypeCheckExpression());
	}
}