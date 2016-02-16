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
package cc.kave.episodes.evaluation.queries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cc.kave.commons.model.episodes.Fact;
import cc.recommenders.datastructures.Tuple;

public class FactsSeparator {
	
	public Tuple<Fact, Set<Fact>> separate(Iterable<Fact> facts) {
		Map<Fact, Integer> orderCounter = new HashMap<Fact, Integer>();
		int numEvents = 0;
		
		for (Fact fact : facts) {
			if (fact.isRelation()) {
				Tuple<Fact, Fact> tuple = fact.getRelationFacts();
				Fact existanceFact = tuple.getFirst();
				if (orderCounter.containsKey(existanceFact)) {
					int counter = orderCounter.get(existanceFact);
					orderCounter.put(existanceFact, counter + 1);
				} else {
					orderCounter.put(existanceFact, 1);
				}
			}
			else {
				numEvents++;
			}
		}
		
		Fact methodDecl = new Fact();
		for (Map.Entry<Fact, Integer> entry : orderCounter.entrySet()) {
			if (entry.getValue() == (numEvents - 1)) {
				methodDecl = entry.getKey();
			}
		}
		
		Set<Fact> bodyFacts = new HashSet<Fact>();
		for (Fact fact : facts) {
			if (!fact.equals(methodDecl) && !fact.isRelation()) {
				bodyFacts.add(fact);
			}
		}
		
		return Tuple.newTuple(methodDecl, bodyFacts);
	}
}
