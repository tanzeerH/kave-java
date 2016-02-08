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
package cc.kave.commons.pointsto.analysis.methods;

import java.util.IdentityHashMap;

import cc.kave.commons.model.names.IMethodName;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.IStatement;

public class EnclosingMethodCollector {

	private IdentityHashMap<IStatement, IMethodName> statementToMethod;

	public EnclosingMethodCollector(ISST sst) {
		EnclosingMethodVisitor visitor = new EnclosingMethodVisitor();
		EnclosingMethodVisitorContext visitorContext = new EnclosingMethodVisitorContext();
		sst.accept(visitor, visitorContext);

		statementToMethod = visitorContext.getMapping();
	}

	public IMethodName getMethod(IStatement stmt) {
		return statementToMethod.get(stmt);
	}

}
