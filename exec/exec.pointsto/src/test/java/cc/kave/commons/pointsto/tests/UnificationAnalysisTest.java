/**
 * Copyright 2016 Simon Reuß
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package cc.kave.commons.pointsto.tests;

import static cc.kave.commons.model.ssts.impl.SSTUtil.assignmentToLocal;
import static cc.kave.commons.model.ssts.impl.SSTUtil.declare;
import static cc.kave.commons.model.ssts.impl.SSTUtil.invocationExpr;
import static cc.kave.commons.model.ssts.impl.SSTUtil.refExpr;
import static cc.kave.commons.model.ssts.impl.SSTUtil.variableReference;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.collect.Iterables;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.names.FieldName;
import cc.kave.commons.model.names.MethodName;
import cc.kave.commons.model.names.TypeName;
import cc.kave.commons.model.names.csharp.CsFieldName;
import cc.kave.commons.model.names.csharp.CsMethodName;
import cc.kave.commons.model.names.csharp.CsTypeName;
import cc.kave.commons.model.ssts.IReference;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.declarations.IVariableDeclaration;
import cc.kave.commons.model.ssts.expressions.assignable.IInvocationExpression;
import cc.kave.commons.model.ssts.expressions.assignable.ILambdaExpression;
import cc.kave.commons.model.ssts.expressions.simple.IReferenceExpression;
import cc.kave.commons.model.ssts.references.IFieldReference;
import cc.kave.commons.model.ssts.statements.IAssignment;
import cc.kave.commons.pointsto.LanguageOptions;
import cc.kave.commons.pointsto.SSTBuilder;
import cc.kave.commons.pointsto.analysis.AbstractLocation;
import cc.kave.commons.pointsto.analysis.Callpath;
import cc.kave.commons.pointsto.analysis.PointerAnalysis;
import cc.kave.commons.pointsto.analysis.QueryContextKey;
import cc.kave.commons.pointsto.analysis.reference.DistinctKeywordReference;
import cc.kave.commons.pointsto.analysis.reference.DistinctReference;
import cc.kave.commons.pointsto.analysis.reference.DistinctVariableReference;
import cc.kave.commons.pointsto.analysis.unification.SteensgaardUnificationAnalysis;
import cc.kave.commons.pointsto.analysis.unification.UnificationAnalysisVisitorContext;

public class UnificationAnalysisTest {

	@Test
	public void testVisitorContext() {
		TestSSTBuilder builder = new TestSSTBuilder();
		TypeName testType = CsTypeName.newTypeName("Test.UnificationContextTest, Test");
		UnificationAnalysisVisitorContext visitorContext = new UnificationAnalysisVisitorContext(
				builder.createContext(builder.createEmptySST(testType)));

		MethodName aCtor = CsMethodName.newMethodName("[?] [UnificationContextTest.A]..ctor()");
		MethodName bCtor = CsMethodName.newMethodName("[?] [UnificationContextTest.B]..ctor()");
		IVariableDeclaration xDecl = declare("x", CsTypeName.UNKNOWN_NAME);
		IVariableDeclaration zDecl = declare("z", CsTypeName.UNKNOWN_NAME);
		IVariableDeclaration aDecl = declare("a", CsTypeName.UNKNOWN_NAME);
		IVariableDeclaration bDecl = declare("b", CsTypeName.UNKNOWN_NAME);
		IVariableDeclaration yDecl = declare("y", CsTypeName.UNKNOWN_NAME);
		IVariableDeclaration cDecl = declare("c", CsTypeName.UNKNOWN_NAME);

		visitorContext.declareVariable(xDecl);
		IInvocationExpression invocation = invocationExpr(aCtor);
		visitorContext.setLastAssignment(assignmentToLocal("x", invocation));
		visitorContext.allocate(visitorContext.getDestinationForExpr(invocation));

		visitorContext.declareVariable(zDecl);
		invocation = invocationExpr(bCtor);
		visitorContext.setLastAssignment(assignmentToLocal("z", invocation));
		visitorContext.allocate(visitorContext.getDestinationForExpr(invocation));

		visitorContext.declareVariable(aDecl);
		visitorContext.copy(variableReference("a"), variableReference("x"));

		visitorContext.declareVariable(bDecl);
		visitorContext.copy(variableReference("b"), variableReference("z"));

		visitorContext.declareVariable(yDecl);
		visitorContext.copy(variableReference("y"), variableReference("x"));
		visitorContext.copy(variableReference("y"), variableReference("z"));

		Map<DistinctReference, AbstractLocation> referenceLocations = visitorContext.getReferenceLocations();
		// all variables point to the same location + this/super location
		assertEquals(2, new HashSet<>(referenceLocations.values()).size());

		visitorContext.declareVariable(cDecl);
		IFieldReference fieldRef = builder.buildFieldReference("y", CsFieldName.newFieldName("[?] [?].f"));
		visitorContext.setLastAssignment(assignmentToLocal("c", refExpr(fieldRef)));
		visitorContext.readField(variableReference("c"), fieldRef);

		referenceLocations = visitorContext.getReferenceLocations();
		AbstractLocation thisLocation = referenceLocations
				.get(new DistinctKeywordReference(LanguageOptions.getInstance().getThisName(), testType));
		assertNotNull(thisLocation);
		AbstractLocation xLocation = referenceLocations.get(new DistinctVariableReference(xDecl));
		assertNotNull(xLocation);
		AbstractLocation yLocation = referenceLocations.get(new DistinctVariableReference(yDecl));
		assertNotNull(yLocation);
		AbstractLocation cLocation = referenceLocations.get(new DistinctVariableReference(cDecl));
		assertNotNull(cLocation);

		assertTrue(xLocation.equals(yLocation));
		assertFalse(yLocation.equals(cLocation));
		assertFalse(thisLocation.equals(xLocation));

	}

	@Test
	public void testStreams() {
		TestSSTBuilder builder = new TestSSTBuilder();
		Context context = builder.createStreamTest();
		TypeName enclosingType = context.getSST().getEnclosingType();

		PointerAnalysis pointerAnalysis = new SteensgaardUnificationAnalysis();
		pointerAnalysis.compute(context);

		FieldName sourceField = CsFieldName.newFieldName(
				"[" + builder.getStringType().getIdentifier() + "] [" + enclosingType.getIdentifier() + "].source");
		Set<AbstractLocation> sourceFieldLocations = pointerAnalysis.query(
				new QueryContextKey(SSTBuilder.fieldReference(sourceField), null, builder.getStringType(), null));
		assertEquals(1, sourceFieldLocations.size());

		IMethodDeclaration openSourceDecl = context.getSST().getNonEntryPoints().iterator().next();
		IAssignment assignment = (IAssignment) openSourceDecl.getBody().get(1);
		IInvocationExpression invocation = (IInvocationExpression) assignment.getExpression();
		IReference firstParameterRef = ((IReferenceExpression) invocation.getParameters().get(0)).getReference();
		Set<AbstractLocation> firstParameterLocations = pointerAnalysis.query(new QueryContextKey(firstParameterRef,
				assignment, builder.getStringType(), new Callpath(openSourceDecl.getName())));
		assertEquals(1, firstParameterLocations.size());
		assertTrue(sourceFieldLocations.equals(firstParameterLocations));

		Set<AbstractLocation> openSourceStreamLocations = pointerAnalysis
				.query(new QueryContextKey(assignment.getReference(), openSourceDecl.getBody().get(2),
						builder.getFileStreamType(), new Callpath(openSourceDecl.getName())));
		assertEquals(1, openSourceStreamLocations.size());

		IMethodDeclaration copyToDecl = null;
		for (IMethodDeclaration decl : context.getSST().getEntryPoints()) {
			if (decl.getName().getName().equals("CopyTo")) {
				copyToDecl = decl;
				break;
			}
		}
		assertNotNull(copyToDecl);

		assignment = (IAssignment) copyToDecl.getBody().get(1);
		Set<AbstractLocation> inputStreamLocations = pointerAnalysis
				.query(new QueryContextKey(assignment.getReference(), assignment, builder.getFileStreamType(),
						new Callpath(copyToDecl.getName())));
		assertEquals(1, inputStreamLocations.size());
		// input = object allocated in OpenSource
		assertTrue(openSourceStreamLocations.equals(inputStreamLocations));
		assertFalse(sourceFieldLocations.equals(inputStreamLocations));

		assignment = (IAssignment) copyToDecl.getBody().get(3);
		Set<AbstractLocation> outputStreamLocations = pointerAnalysis
				.query(new QueryContextKey(assignment.getReference(), assignment, builder.getFileStreamType(),
						new Callpath(copyToDecl.getName())));
		assertEquals(1, outputStreamLocations.size());
		// input != output
		assertFalse(inputStreamLocations.equals(outputStreamLocations));
	}

	@Test
	public void testDelegates() {
		TestSSTBuilder builder = new TestSSTBuilder();
		Context context = builder.createDelegateTest();

		PointerAnalysis pointerAnalysis = new SteensgaardUnificationAnalysis();
		pointerAnalysis.compute(context);

		IMethodDeclaration fooDecl = context.getSST().getNonEntryPoints().iterator().next();
		IMethodDeclaration entry1Decl = Iterables.find(context.getSST().getEntryPoints(),
				md -> md.getName().getName().equals("entry1"));
		IMethodDeclaration entry2Decl = Iterables.find(context.getSST().getEntryPoints(),
				md -> md.getName().getName().equals("entry2"));
		assertNotNull(fooDecl);
		assertNotNull(entry1Decl);
		assertNotNull(entry2Decl);

		Callpath entry1Callpath = new Callpath(entry1Decl.getName());
		IVariableDeclaration entry1ArgDecl = (IVariableDeclaration) entry1Decl.getBody().get(2);
		TypeName objectType = entry1ArgDecl.getType();
		IAssignment entry1InvokeFunAssignment = (IAssignment) entry1Decl.getBody().get(5);
		Set<AbstractLocation> entry1ArgInvocationLocations = pointerAnalysis.query(
				new QueryContextKey(variableReference("arg"), entry1InvokeFunAssignment, objectType, entry1Callpath));
		assertEquals(1, entry1ArgInvocationLocations.size());

		Set<AbstractLocation> fooParameterLocations = pointerAnalysis
				.query(new QueryContextKey(variableReference("x"), null, objectType, new Callpath(fooDecl.getName())));
		assertThat(entry1ArgInvocationLocations, Matchers.is(fooParameterLocations));

		Callpath entry2Callpath = new Callpath(entry2Decl.getName());
		IAssignment entry2InvokeFunAssignment = (IAssignment) entry2Decl.getBody().get(5);
		Set<AbstractLocation> entry2ArgInvocationLocations = pointerAnalysis.query(
				new QueryContextKey(variableReference("arg"), entry2InvokeFunAssignment, objectType, entry2Callpath));
		assertEquals(1, entry2ArgInvocationLocations.size());

		ILambdaExpression lambda = (ILambdaExpression) ((IAssignment) entry2Decl.getBody().get(1)).getExpression();
		Set<AbstractLocation> lambdaParameterLocations = pointerAnalysis.query(
				new QueryContextKey(variableReference("x"), lambda.getBody().get(1), objectType, entry2Callpath));
		assertThat(entry2ArgInvocationLocations, Matchers.is(lambdaParameterLocations));

		// the parameter of 'foo' and the lambda get unified by the 'arg0' parameter of String.Format
		assertThat(fooParameterLocations, Matchers.is(lambdaParameterLocations));

		// check that the 'fun' variables of entry1 and entry2 do not refer to the same object
		TypeName delegateType = ((IVariableDeclaration) entry1Decl.getBody().get(0)).getType();
		Set<AbstractLocation> entry1FunLocations = pointerAnalysis.query(
				new QueryContextKey(variableReference("fun"), entry1InvokeFunAssignment, delegateType, entry1Callpath));
		Set<AbstractLocation> entry2FunLocations = pointerAnalysis.query(
				new QueryContextKey(variableReference("fun"), entry2InvokeFunAssignment, delegateType, entry2Callpath));
		assertEquals(1, entry1FunLocations.size());
		assertEquals(1, entry2FunLocations.size());
		assertThat(entry1FunLocations, Matchers.not(entry2FunLocations));

	}
}
