package cc.kave.episodes.evaluation.queries;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.util.MathUtils;

import com.google.common.collect.Sets;

import cc.kave.commons.model.episodes.Fact;
import cc.kave.episodes.model.Episode;
import cc.recommenders.datastructures.Tuple;

public class QueryGeneratorByPercentage {

	private SubsetsGenerator generator = new SubsetsGenerator();
	private Separator separator = new Separator();
	private Tuple<Fact, Set<Fact>> declInv = Tuple.newTuple(new Fact(), Sets.newHashSet());
	private int numInvs = 0;
	
	public Map<Double, Set<Episode>> generateQueries(Episode target) {
		preprocessing(target);
		Map<Double, Set<Episode>> queries = new LinkedHashMap<Double, Set<Episode>>();
		Map<Double, Integer> removals = calcPercNumbers(numInvs);
		
		for (Map.Entry<Double, Integer> entry : removals.entrySet()) {
			int selectionLength = numInvs - entry.getValue();
			Set<Set<Fact>> currentSubsets = generator.generateSubsets(declInv.getSecond(), selectionLength);
			for (Set<Fact> subset : currentSubsets) {
				Episode query = createQuery(target, declInv.getFirst(), subset);
				if (queries.containsKey(entry.getKey())) {
					queries.get(entry.getKey()).add(query);
				} else {
					queries.put(entry.getKey(), Sets.newHashSet(query));
				}
			}
		}
		return queries;
	}
	
	private void preprocessing(Episode target) {
		assertTrue(target.getNumEvents() > 2, "Not valid episode for query generation!");
		declInv = separator.separateFacts(target);
		numInvs = declInv.getSecond().size();
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

	private Map<Double, Integer> calcPercNumbers(int size) {
		Map<Double, Integer> removals = new LinkedHashMap<Double, Integer>();
		
		for (double perc = 0.10; perc < 1.0; perc += 0.10) {
			int p = (int) Math.ceil(perc * (double) size);
			if (!removals.values().contains(p) && (p > 0.0 && p < size)) {
				removals.put(MathUtils.round(perc, 2), p);
			}
		}
		return removals;
	}
}
