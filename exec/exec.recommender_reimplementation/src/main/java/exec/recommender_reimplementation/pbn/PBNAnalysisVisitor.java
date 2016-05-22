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
package exec.recommender_reimplementation.pbn;

import static cc.kave.commons.pointsto.extraction.CoReNameConverter.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import cc.kave.commons.model.names.IMethodName;
import cc.kave.commons.model.names.IParameterName;
import cc.kave.commons.model.names.ITypeName;
import cc.kave.commons.model.ssts.IReference;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.blocks.ITryBlock;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.expressions.IAssignableExpression;
import cc.kave.commons.model.ssts.expressions.ISimpleExpression;
import cc.kave.commons.model.ssts.expressions.assignable.IInvocationExpression;
import cc.kave.commons.model.ssts.expressions.simple.IReferenceExpression;
import cc.kave.commons.model.ssts.impl.visitor.AbstractTraversingNodeVisitor;
import cc.kave.commons.model.ssts.references.IFieldReference;
import cc.kave.commons.model.ssts.references.IVariableReference;
import cc.kave.commons.model.ssts.statements.IAssignment;
import cc.kave.commons.model.typeshapes.IMethodHierarchy;
import cc.kave.commons.model.typeshapes.ITypeHierarchy;
import cc.kave.commons.pointsto.analysis.AbstractLocation;
import cc.kave.commons.pointsto.analysis.PointsToContext;
import cc.kave.commons.pointsto.analysis.PointsToQuery;
import cc.kave.commons.pointsto.analysis.PointsToQueryBuilder;
import cc.kave.commons.pointsto.analysis.types.TypeCollector;
import cc.kave.commons.utils.SSTNodeHierarchy;
import cc.recommenders.names.ICoReTypeName;
import cc.recommenders.usages.CallSite;
import cc.recommenders.usages.CallSites;
import cc.recommenders.usages.DefinitionSite;
import cc.recommenders.usages.DefinitionSites;
import cc.recommenders.usages.Query;
import cc.recommenders.usages.Usage;

public class PBNAnalysisVisitor extends AbstractTraversingNodeVisitor<List<Usage>, Object> {

	private PointsToContext pointsToContext;
	private PointsToQueryBuilder queryBuilder;
	private SSTNodeHierarchy sstNodeHierarchy;
	private TypeCollector typeCollector;

	private IMethodDeclaration lastVisitedMethodDeclaration;

	public PBNAnalysisVisitor(PointsToContext pointsToContext) {
		this.pointsToContext = pointsToContext;
		queryBuilder = new PointsToQueryBuilder(pointsToContext);
		sstNodeHierarchy = new SSTNodeHierarchy(pointsToContext.getSST());
		typeCollector = new TypeCollector(pointsToContext);
	}

	@Override
	public Object visit(IMethodDeclaration decl, List<Usage> context) {
		lastVisitedMethodDeclaration = decl;
		return super.visit(decl, context);
	}

	@Override
	public Object visit(IInvocationExpression expr, List<Usage> usages) {
		// Handle Receiver first
		ICoReTypeName type = findTypeForVarReference(expr);
		Optional<Usage> usage = usageListContainsType(usages, type);
		handleObjectInstance(expr, usages, usage, -1, null);

		// Handle Parameters
		List<ISimpleExpression> parameters = expr.getParameters();
		List<ITypeName> parameterTypes = createTypeListFromParameters(parameters);
		for (int i = 0; i < parameterTypes.size(); i++) {
			ITypeName parameterType = parameterTypes.get(i);
			usage = usageListContainsType(usages, convert(parameterType));
			int parameterIndex = getIndexOfParameter(parameters, parameterType);
			handleObjectInstance(expr, usages, usage, parameterIndex, parameterType);
		}

		return super.visit(expr, usages);
	}

	@Override
	public Object visit(ITryBlock block, List<Usage> context) {
		visit(block.getBody(), context);
		// ignores Catch Block because exception-handling is ignored in analysis
		visit(block.getFinally(), context);
		return null;
	}

	public void handleObjectInstance(IInvocationExpression expr, List<Usage> usages, Optional<Usage> usage,
			int parameterIndex, ITypeName parameterType) {
		if (usage.isPresent()) {
			addCallSite((Query) usage.get(), expr, parameterIndex);
		} else {
			Query newUsage = createNewObjectUsage(expr, parameterType);
			addClassContext(newUsage);
			addMethodContext(newUsage);
			if (!addDefinitionSite(newUsage, expr, parameterIndex)) return;
			addCallSite(newUsage, expr, parameterIndex);
			usages.add(newUsage);
		}
	}

	public int getIndexOfParameter(List<ISimpleExpression> parameters, ITypeName parameterType) {
		for (int i = 0; i < parameters.size(); i++) {
			ISimpleExpression expr = parameters.get(i);
			if (expr instanceof IReferenceExpression) {
				IReferenceExpression refExpr = (IReferenceExpression) expr;
				if (typeCollector.getType(refExpr.getReference()).equals(parameterType))
					return i;
			}

		}
		// TODO: Is this branch possible?
		return -1;
	}

	public List<ITypeName> createTypeListFromParameters(List<ISimpleExpression> parameters) {
		List<ITypeName> typeList = new ArrayList<ITypeName>();
		for (ISimpleExpression parameter : parameters) {
			if (parameter instanceof IReferenceExpression) {
				IReferenceExpression refExpr = (IReferenceExpression) parameter;
				IReference reference = refExpr.getReference();
				ITypeName type = typeCollector.getType(reference);
				if (type != null)
					typeList.add(type);
			}
		}
		return typeList;
	}

	private void visit(List<IStatement> body, List<Usage> context) {
		for (IStatement stmt : body) {
			stmt.accept(this, context);
		}
	}

	public void addCallSite(Query usage, IInvocationExpression expr, int argIndex) {
		if (argIndex > -1) {
			CallSite callSite = CallSites.createParameterCallSite(convert(expr.getMethodName()), argIndex);
			usage.addCallSite(callSite);
		} else {
			IMethodName firstMethodName = findFirstMethodName(expr.getMethodName());
			CallSite callSite = CallSites.createReceiverCallSite(convert(firstMethodName));
			usage.addCallSite(callSite);
		}
	}

	private void addClassContext(Query newUsage) {
		ITypeName classType = findClassContext();
		newUsage.setClassContext(convert(classType));
	}

	private boolean addDefinitionSite(Query newUsage, IInvocationExpression expr, int parameterIndex) {
		if (isCallToSuperClass(expr)) {
			newUsage.setDefinition(DefinitionSites.createDefinitionByThis());
			return true;
		}
		int parameterOfEntryPointIndex = getParameterIndexInEntryPoint(expr, parameterIndex);
		if (parameterOfEntryPointIndex > -1) {
			newUsage.setDefinition(DefinitionSites.createDefinitionByParam(convert(lastVisitedMethodDeclaration.getName()), parameterOfEntryPointIndex));
			return true;
		}
		DefinitionSite definitionSite = findDefinitionSiteByReference(expr, parameterIndex, lastVisitedMethodDeclaration);
		if(definitionSite != null) {
			newUsage.setDefinition(definitionSite);
			return true;
		}
		return false;
	}

	public DefinitionSite findDefinitionSiteByReference(IInvocationExpression expr, int parameterIndex,
			IMethodDeclaration methodDecl) {
		IReference varRef = parameterIndex == -1 ? expr.getReference()
				: ((IReferenceExpression) expr.getParameters().get(parameterIndex)).getReference();
		
		List<IStatement> body = methodDecl.getBody();
		PointsToQuery queryForVarReference = queryBuilder.newQuery(varRef, (IStatement) sstNodeHierarchy.getParent(expr));
		Set<AbstractLocation> varRefLocations = pointsToContext.getPointerAnalysis().query(queryForVarReference);
		
		List<IAssignment> assignments = getAssignmentList(body);
		
		for (IAssignment assignment : assignments) {
			// TODO: maybe traverse backwards to use last assignment for reference
			PointsToQuery queryForLeftSide = queryBuilder.newQuery(assignment.getReference(), assignment);
			Set<AbstractLocation> leftSideLocations = pointsToContext.getPointerAnalysis().query(queryForLeftSide);
			if(varRefLocations.equals(leftSideLocations)) {
				IAssignableExpression assignExpr = assignment.getExpression();
				DefinitionSite fieldSite = tryGetFieldDefinitionSite(assignExpr);
				if(fieldSite != null) return fieldSite;
				
				DefinitionSite invocationSite = tryGetInvocationDefinition(assignExpr);
				if(invocationSite != null) return invocationSite;
				
				break;
			}
		}
		return null;
	}

	public DefinitionSite tryGetInvocationDefinition(IAssignableExpression assignExpr) {
		if(assignExpr instanceof IInvocationExpression) {
			IInvocationExpression invocation = (IInvocationExpression) assignExpr;
			IMethodName methodName = invocation.getMethodName();
			if(methodName.isConstructor()) {
				return DefinitionSites.createDefinitionByConstructor(convert(methodName));
			}
			return DefinitionSites.createDefinitionByReturn(convert(methodName));
		}
		return null;
	}

	public List<IAssignment> getAssignmentList(List<IStatement> body) {
		return body
				.stream()
				.filter(statement -> statement instanceof IAssignment)
				.map(statement -> (IAssignment) statement)
				.collect(Collectors.toList());
	}

	public DefinitionSite tryGetFieldDefinitionSite(IAssignableExpression assignExpr) {
		if(assignExpr instanceof IReferenceExpression) {
			IReferenceExpression refExpr = (IReferenceExpression) assignExpr;
			IReference reference = refExpr.getReference();
			if(reference instanceof IFieldReference) {
				IFieldReference fieldRef = (IFieldReference) reference;
				if(fieldRef.getReference().getIdentifier().equals("this")) {
					return DefinitionSites.createDefinitionByField(convert(fieldRef.getFieldName()));
				}
			}
		}
		return null;
	}

	public int getParameterIndexInEntryPoint(IInvocationExpression expr, int parameterIndex) {
		IReference reference = parameterIndex == -1 ? expr.getReference() : ((IReferenceExpression) expr.getParameters().get(parameterIndex)).getReference();
		if(!(reference instanceof IVariableReference)) return -1;
		IVariableReference varRef = (IVariableReference) reference;
		List<IParameterName> parameterNames = lastVisitedMethodDeclaration.getName().getParameters();
		for (int i = 0; i < parameterNames.size(); i++) {
			IParameterName parameterName = parameterNames.get(i);
			if(parameterName.getName().equals(varRef.getIdentifier()))
				return i;
		}
		return -1;
	}

	public boolean isCallToSuperClass(IInvocationExpression expr) {
		if (!expr.getReference().getIdentifier().equals("this"))
			return false;
		IMethodName methodName = expr.getMethodName();
		return pointsToContext.getSST().getMethods().stream()
				.noneMatch(methodDecl -> methodDecl.getName().equals(methodName));
	}

	private void addMethodContext(Query newUsage) {
		IMethodName firstMethodName = findFirstMethodName(lastVisitedMethodDeclaration.getName());
		newUsage.setMethodContext(convert(firstMethodName));
	}

	public ITypeName findClassContext() {
		ITypeHierarchy typeHierarchy = pointsToContext.getTypeShape().getTypeHierarchy();
		// TODO: only use super class or also add interfaces as class context?
		ITypeName classType = typeHierarchy.hasSuperclass() ? typeHierarchy.getExtends().getElement() : typeHierarchy
					.getElement();		
		return classType;
	}

	public IMethodName findFirstMethodName(IMethodName methodName) {
		Set<IMethodHierarchy> methodHierarchies = pointsToContext.getTypeShape().getMethodHierarchies();
		Optional<IMethodHierarchy> methodHierarchy = methodHierarchies.stream()
				.filter(mh -> mh.getElement().equals(methodName)).findAny();
		IMethodName firstMethodName = null;
		if (methodHierarchy.isPresent()) {
			firstMethodName = methodHierarchy.get().getFirst();
		}
		return firstMethodName != null ? firstMethodName : methodName;
	}

	public Query createNewObjectUsage(IInvocationExpression expr, ITypeName parameterType) {
		Query result = new Query();
		ICoReTypeName type = parameterType == null ? findTypeForVarReference(expr) : convert(parameterType);
		result.setType(type);

		return result;
	}

	public ICoReTypeName findTypeForVarReference(IInvocationExpression invocation) {
		return convert(typeCollector.getType(invocation.getReference()));
	}

	public Optional<Usage> usageListContainsType(List<Usage> usages, ICoReTypeName type) {
		return usages.stream().filter(usage -> usage.getType().equals(type)).findAny();
	}
}