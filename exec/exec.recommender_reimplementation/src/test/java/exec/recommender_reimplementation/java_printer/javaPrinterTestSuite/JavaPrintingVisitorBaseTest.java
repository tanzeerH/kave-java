/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package exec.recommender_reimplementation.java_printer.javaPrinterTestSuite;

import static cc.kave.commons.model.ssts.impl.SSTUtil.declareMethod;

import java.util.Arrays;

import org.junit.Assert;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IFieldName;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.codeelements.IParameterName;
import cc.kave.commons.model.naming.codeelements.IPropertyName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.model.ssts.IExpression;
import cc.kave.commons.model.ssts.IReference;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.declarations.IFieldDeclaration;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.declarations.IPropertyDeclaration;
import cc.kave.commons.model.ssts.expressions.ISimpleExpression;
import cc.kave.commons.model.ssts.expressions.assignable.IInvocationExpression;
import cc.kave.commons.model.ssts.impl.SST;
import cc.kave.commons.model.ssts.impl.declarations.FieldDeclaration;
import cc.kave.commons.model.ssts.impl.declarations.MethodDeclaration;
import cc.kave.commons.model.ssts.impl.declarations.PropertyDeclaration;
import cc.kave.commons.model.ssts.impl.expressions.assignable.InvocationExpression;
import cc.kave.commons.model.ssts.impl.expressions.simple.ConstantValueExpression;
import cc.kave.commons.model.ssts.impl.references.FieldReference;
import cc.kave.commons.model.ssts.impl.references.VariableReference;
import cc.kave.commons.model.ssts.references.IFieldReference;
import cc.kave.commons.model.ssts.visitor.ISSTNode;
import cc.kave.commons.model.typeshapes.ITypeShape;
import cc.kave.commons.utils.sstprinter.SSTPrintingVisitor;
import exec.recommender_reimplementation.java_printer.JavaPrintingContext;
import exec.recommender_reimplementation.java_printer.JavaPrintingVisitor;

public class JavaPrintingVisitorBaseTest {
	protected SSTPrintingVisitor sut;

	protected void assertPrintWithCustomContext(ISSTNode sst, ITypeShape typeShape, String expected) {
		JavaPrintingContext context = new JavaPrintingContext();
		context.setTypeShape(typeShape);
		int indentationLevel = context.indentationLevel;
		if (sut == null) {
			sut = new JavaPrintingVisitor(sst, false);
		}
		sst.accept(sut, context);
		String actual = context.toString();
		actual = removeImports(actual);
		Assert.assertEquals(expected, actual);
		Assert.assertEquals(indentationLevel, context.indentationLevel);
	}

	private String removeImports(String actual) {
		String removedImports = actual.replaceAll("( )*import (\\w+\\.|\\w+;)+\\n", "");
		return removedImports;
	}

	protected void assertPrintWithCustomContext(ISSTNode sst, JavaPrintingContext context, String expected) {
		int indentationLevel = context.indentationLevel;
		if (sut == null) {
			sut = new JavaPrintingVisitor(sst, false);
		}
		sst.accept(sut, context);
		String actual = context.toString();
		actual = removeImports(actual);
		Assert.assertEquals(expected, actual);
		Assert.assertEquals(indentationLevel, context.indentationLevel);
	}

	protected void assertPrintWithCustomContext(ISSTNode sst, ITypeShape typeShape, String... expectedLines) {
		sut = new JavaPrintingVisitor(sst,false);
		assertPrintWithCustomContext(sst, typeShape, String.join("\n", expectedLines));
	}

	protected void assertPrintWithCustomContext(ISSTNode sst, JavaPrintingContext context, String... expectedLines) {
		sut = new JavaPrintingVisitor(sst,false);
		assertPrintWithCustomContext(sst, context, String.join("\n", expectedLines));
	}
	
	protected void assertPrintWithPublicModifier(ISSTNode sst, String... expectedLines) {
		sut = new JavaPrintingVisitor(sst,true);
		assertPrintWithCustomContext(sst, new JavaPrintingContext(), String.join("\n", expectedLines));
	}

	protected void assertPrint(ISSTNode sst, String... expectedLines) {
		testPrintingWithoutIndentation(sst, expectedLines);

		// Expressions and references can't be indented
		if (!(sst instanceof IExpression || sst instanceof IReference)) {
			testPrintingWithIndentation(sst, expectedLines);
		}
	}

	private void testPrintingWithoutIndentation(ISSTNode sst, String[] expectedLines) {
		JavaPrintingContext context = new JavaPrintingContext();
		context.setIndentationLevel(0);
		assertPrintWithCustomContext(sst, context, expectedLines);
	}

	private void testPrintingWithIndentation(ISSTNode sst, String... expectedLines) {
		String[] indentedLines = new String[expectedLines.length];
		for (int i = 0; i < expectedLines.length; i++) {
			indentedLines[i] = Strings.isNullOrEmpty(expectedLines[i]) ? expectedLines[i] : "    " + expectedLines[i];
		}
		JavaPrintingContext context = new JavaPrintingContext();
		context.setIndentationLevel(1);
		assertPrintWithCustomContext(sst, context, indentedLines);
	}
	
	protected IFieldName field(ITypeName valType, ITypeName declType, String fieldName) {
		String field = String.format("[%1$s] [%2$s].%3$s", valType.getIdentifier(), declType.getIdentifier(),
				fieldName);
		return Names.newField(field);
	}
	
	protected static IMethodName method(ITypeName returnType, ITypeName declType, String simpleName,
			IParameterName... parameters) {
		String parameterStr = Joiner.on(", ").join(
				Arrays.asList(parameters).stream().map(p -> p.getIdentifier()).toArray());
		String methodIdentifier = String.format("[%1$s] [%2$s].%3$s(%4$s)", returnType.getIdentifier(),
				declType.getIdentifier(), simpleName,
				parameterStr);
		return Names.newMethod(methodIdentifier);
	}

	protected IParameterName parameter(ITypeName valType, String paramName) {
		String param = String.format("[%1$s] %2$s", valType.getIdentifier(), paramName);
		return Names.newParameter(param);
	}

	protected ITypeName type(String simpleName) {
		return Names.newType(simpleName + ",P1");
	}

	protected ConstantValueExpression constant(String value) {
		ConstantValueExpression expr = new ConstantValueExpression();
		expr.setValue(value);
		return expr;
	}
	
	protected IFieldReference fieldRef(String identifier, IFieldName fieldName) {
		FieldReference fieldReference = new FieldReference();
		fieldReference.setFieldName(fieldName);
		fieldReference.setReference(varRef(identifier));
		return fieldReference;
	}
	
	protected IFieldDeclaration fieldDecl(IFieldName fieldName) {
		FieldDeclaration fieldDecl = new FieldDeclaration();
		fieldDecl.setName(fieldName);
		return fieldDecl;
	}
	
	protected IPropertyDeclaration propertyDecl(IPropertyName propertyName) {
		PropertyDeclaration propertyDecl = new PropertyDeclaration();
		propertyDecl.setName(propertyName);
		return propertyDecl;
	}
	
	protected IMethodDeclaration methodDecl(IMethodName methodName, IStatement... statements) {
		MethodDeclaration methodDecl = new MethodDeclaration();
		methodDecl.setName(methodName);
		methodDecl.setBody(Lists.newArrayList(statements));
		return methodDecl;
	}

	protected IInvocationExpression invocation(String identifier, IMethodName methodName,
			ISimpleExpression... parameters) {
		InvocationExpression invocation = new InvocationExpression();
		invocation.setReference(varRef(identifier));
		invocation.setMethodName(methodName);
		invocation.setParameters(Lists.newArrayList(parameters));
		return invocation;
	}

	protected VariableReference varRef(String identifier) {
		VariableReference ref = new VariableReference();
		ref.setIdentifier(identifier);
		return ref;
	}
	
	protected SST defaultSST(ITypeName enclosingType, IStatement... statements) {
		SST sst = new SST();
		sst.setEnclosingType(enclosingType);
		IMethodDeclaration methodDecl = declareMethod(statements);
		sst.getMethods().add(methodDecl);
		return sst;
	}
	
	protected SST defaultSST(IStatement... statements) {
		SST sst = new SST();
		IMethodDeclaration methodDecl = declareMethod(statements);
		sst.getMethods().add(methodDecl);
		return sst;
	}
}
