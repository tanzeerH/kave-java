/**
 * Copyright 2015 Waldemar Graf
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

package cc.kave.eclipse.commons.analysis.completiontarget;

import org.eclipse.jdt.core.dom.ASTNode;

public class CompletionTargetMarker {

	private ASTNode AffectedNode;
	private CompletionCase Case;

	public ASTNode getAffectedNode() {
		return AffectedNode;
	}

	public void setAffectedNode(ASTNode affectedNode) {
		AffectedNode = affectedNode;
	}

	public CompletionCase getCase() {
		return Case;
	}

	public void setCase(CompletionCase case1) {
		Case = case1;
	}

	public enum CompletionCase {
		Undefined, EmptyCompletionBefore, EmptyCompletionAfter, InBody, InElse, InFinally
	}
}
