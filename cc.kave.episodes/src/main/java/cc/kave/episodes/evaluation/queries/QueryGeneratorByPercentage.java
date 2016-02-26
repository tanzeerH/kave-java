package cc.kave.episodes.evaluation.queries;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.util.Set;

import com.google.common.collect.Sets;

import cc.kave.commons.model.episodes.Fact;
import cc.kave.episodes.model.Episode;
import cc.recommenders.datastructures.Tuple;

public class QueryGeneratorByPercentage {

	private SubsetsGenerator generator = new SubsetsGenerator();
	private Separator separator = new Separator();
	
	public Set<Episode> generateQueries(Episode target, double percentage) {
		assertTrue(target.getNumEvents() > 2, "Not valid episode for query generation!");
		
		Set<Episode> queries = Sets.newHashSet();
		Tuple<Fact, Set<Fact>> declInv = separator.separateFacts(target);
		
		int numRemoved = calculateRemovals(declInv.getSecond().size(), percentage);
		int numInvs = declInv.getSecond().size();
		Set<Set<Fact>> subsets = generator.generateSubsets(declInv.getSecond(), numInvs - numRemoved);
		
		for (Set<Fact> subset : subsets) {
			Episode query = createQuery(target, declInv.getFirst(), subset);
			queries.add(query);
		}
		return queries;
	}

	private Episode createQuery(Episode target, Fact methodDecl, Set<Fact> subset) {
		Episode query = new Episode();
		query.addFact(methodDecl);
		for (Fact fact : subset) {
			query.addFact(fact);
			query.addFact(new Fact(methodDecl, fact));
		}
		
		for (Fact fact : target.getFacts()) {
			if (fact.isRelation()) {
				Tuple<Fact, Fact> pairFacts = fact.getRelationFacts();
				if (subset.contains(pairFacts.getFirst()) && subset.contains(pairFacts.getSecond())) {
					query.addFact(fact);
				}
			}
		}
		return query;
	}

	private int calculateRemovals(int size, double percentage) {
		return (int) Math.ceil(percentage * (double) size) ;
	}

}
