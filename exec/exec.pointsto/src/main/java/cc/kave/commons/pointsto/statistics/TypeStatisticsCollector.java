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
package cc.kave.commons.pointsto.statistics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.pointsto.io.IOHelper;
import cc.recommenders.names.ITypeName;
import cc.recommenders.names.Names;
import cc.recommenders.usages.Usage;

public class TypeStatisticsCollector implements UsageStatisticsCollector {

	private static final char SEPARATOR = ' ';

	private Predicate<Usage> usageFilter;

	private Map<ITypeName, Statistics> typeStatistics = new HashMap<>();

	public TypeStatisticsCollector(Predicate<Usage> usageFilter) {
		this.usageFilter = usageFilter;
	}

	@Override
	public UsageStatisticsCollector create() {
		return new TypeStatisticsCollector(usageFilter);
	}

	@Override
	public void merge(UsageStatisticsCollector other) {
		TypeStatisticsCollector otherTypeCollector = (TypeStatisticsCollector) other;

		synchronized (typeStatistics) {
			for (Map.Entry<ITypeName, Statistics> entry : otherTypeCollector.typeStatistics.entrySet()) {
				Statistics otherStats = entry.getValue();
				Statistics myStats = typeStatistics.get(entry.getKey());
				if (myStats == null) {
					myStats = new Statistics();
					typeStatistics.put(entry.getKey(), myStats);
				}

				myStats.numUsages += otherStats.numUsages;
				myStats.numFilteredUsages = otherStats.numFilteredUsages;
				myStats.sumCallsites += otherStats.sumCallsites;
				myStats.sumFilteredCallsites += otherStats.sumFilteredCallsites;
			}
		}
	}

	@Override
	public void onProcessContext(Context context) {

	}

	@Override
	public void onEntryPointUsagesExtracted(IMethodDeclaration entryPoint, List<? extends Usage> usages) {
		process(usages);
	}

	@Override
	public void process(List<? extends Usage> usages) {
		for (Usage usage : usages) {
			Statistics stats = typeStatistics.get(usage.getType());
			if (stats == null) {
				stats = new Statistics();
				typeStatistics.put(usage.getType(), stats);
			}

			++stats.numUsages;
			stats.sumCallsites += usage.getAllCallsites().size();

			if (usageFilter.test(usage)) {
				++stats.numFilteredUsages;
				stats.sumFilteredCallsites += usage.getAllCallsites().size();
			}
		}
	}

	@Override
	public void output(Path file) throws IOException {
		List<Map.Entry<ITypeName, Statistics>> entries = new ArrayList<>(typeStatistics.entrySet());
		entries.sort(new Comparator<Map.Entry<ITypeName, Statistics>>() {

			@Override
			public int compare(Entry<ITypeName, Statistics> o1, Entry<ITypeName, Statistics> o2) {
				int diff = o2.getValue().numUsages - o1.getValue().numUsages;

				if (diff == 0) {
					return o1.getKey().getIdentifier().compareTo(o2.getKey().getIdentifier());
				}

				return diff;
			}
		});

		IOHelper.createParentDirs(file);
		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			for (Map.Entry<ITypeName, Statistics> entry : entries) {
				writer.append(Names.vm2srcQualifiedType(entry.getKey()));
				writer.append(SEPARATOR);

				Statistics stats = entry.getValue();
				writer.append(Integer.toString(stats.numUsages));
				writer.append(SEPARATOR);
				writer.append(Integer.toString(stats.numFilteredUsages));
				writer.append(SEPARATOR);
				writer.append(String.format(Locale.US, "%.1f", calcAverage(stats.sumCallsites, stats.numUsages)));
				writer.append(SEPARATOR);
				writer.append(String.format(Locale.US, "%.1f",
						calcAverage(stats.sumFilteredCallsites, stats.numFilteredUsages)));
				writer.newLine();
			}
		}

	}

	private double calcAverage(long value, int size) {
		if (size == 0) {
			return 0;
		} else {
			return value / (double) size;
		}
	}

	private static class Statistics {
		int numUsages;
		int numFilteredUsages;
		long sumCallsites;
		long sumFilteredCallsites;
	}

}
