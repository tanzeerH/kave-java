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
package cc.kave.commons.pointsto.analysis;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.ssts.IReference;
import cc.kave.commons.model.ssts.IStatement;

/**
 * A convenience class for building {@link QueryContextKey} objects for a {@link PointerAnalysis}.
 * 
 * Uses the provided arguments and its internal information about the associated {@link Context} to create a complete
 * {@link QueryContextKey}.
 *
 */
public class PointsToQueryBuilder {

	private Context context;

	public PointsToQueryBuilder(Context context) {
		this.context = context;
	}

	public QueryContextKey newQuery(IReference reference, IStatement stmt) {
		throw new RuntimeException("Not implemented yet");
	}
}