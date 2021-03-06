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
package cc.kave.commons.pointsto.analysis.unification;

import cc.kave.commons.model.naming.codeelements.IPropertyName;
import cc.kave.commons.model.ssts.references.IEventReference;
import cc.kave.commons.model.ssts.references.IFieldReference;
import cc.kave.commons.model.ssts.references.IIndexAccessReference;
import cc.kave.commons.model.ssts.references.IMethodReference;
import cc.kave.commons.model.ssts.references.IPropertyReference;
import cc.kave.commons.model.ssts.references.IVariableReference;
import cc.kave.commons.pointsto.analysis.references.DistinctEventReference;
import cc.kave.commons.pointsto.analysis.references.DistinctFieldReference;
import cc.kave.commons.pointsto.analysis.references.DistinctIndexAccessReference;
import cc.kave.commons.pointsto.analysis.references.DistinctPropertyReference;
import cc.kave.commons.pointsto.analysis.unification.locations.ReferenceLocation;
import cc.kave.commons.pointsto.analysis.visitors.FailSafeNodeVisitor;

class SourceLocationVisitor extends FailSafeNodeVisitor<UnificationAnalysisVisitorContext, ReferenceLocation> {

	@Override
	public ReferenceLocation visit(IVariableReference varRef, UnificationAnalysisVisitorContext context) {
		return context.getLocation(varRef);
	}

	@Override
	public ReferenceLocation visit(IFieldReference fieldRef, UnificationAnalysisVisitorContext context) {
		ReferenceLocation tempLoc = context.createSimpleReferenceLocation();
		DistinctFieldReference distRef = (DistinctFieldReference) context.getDistinctReference(fieldRef);
		context.readMember(tempLoc, distRef);
		return tempLoc;
	}

	@Override
	public ReferenceLocation visit(IPropertyReference propertyRef, UnificationAnalysisVisitorContext context) {
		ReferenceLocation tempLoc = context.createSimpleReferenceLocation();

		IPropertyName property = propertyRef.getPropertyName();
		if (context.treatPropertyAsField(propertyRef)) {
			DistinctPropertyReference distRef = (DistinctPropertyReference) context.getDistinctReference(propertyRef);
			context.readMember(tempLoc, distRef);
		} else {
			ReferenceLocation returnLocation = context.getOrCreateReturnLocation(property);
			context.alias(tempLoc, returnLocation);
		}

		return tempLoc;
	}

	@Override
	public ReferenceLocation visit(IIndexAccessReference indexAccessRef, UnificationAnalysisVisitorContext context) {
		ReferenceLocation tempLoc = context.createSimpleReferenceLocation();
		DistinctIndexAccessReference distRef = (DistinctIndexAccessReference) context
				.getDistinctReference(indexAccessRef);
		context.readArray(tempLoc, distRef);
		return tempLoc;
	}

	@Override
	public ReferenceLocation visit(IMethodReference methodRef, UnificationAnalysisVisitorContext context) {
		ReferenceLocation tempLoc = context.createSimpleReferenceLocation();
		context.storeFunction(tempLoc, methodRef);
		return tempLoc;
	}

	@Override
	public ReferenceLocation visit(IEventReference eventRef, UnificationAnalysisVisitorContext context) {
		ReferenceLocation tempLoc = context.createSimpleReferenceLocation();
		DistinctEventReference distRef = (DistinctEventReference) context.getDistinctReference(eventRef);
		context.readMember(tempLoc, distRef);
		return tempLoc;
	}
}
