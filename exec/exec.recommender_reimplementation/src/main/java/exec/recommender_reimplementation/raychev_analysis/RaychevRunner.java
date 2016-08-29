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
package exec.recommender_reimplementation.raychev_analysis;

import static org.apache.commons.io.FileUtils.writeStringToFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.completionevents.Context;
import exec.recommender_reimplementation.ContextReader;
import exec.recommender_reimplementation.java_printer.JavaClassPathGenerator;
import exec.recommender_reimplementation.java_printer.printer.JavaPrinter;
import exec.recommender_reimplementation.raychev_analysis.QueryGenerator.QueryStrategy;

public class RaychevRunner {
	public static final Path FOLDERPATH = Paths.get("C:\\SST Datasets\\NewTestset");
	public static final Path QUERY_FOLDER_PATH = Paths.get("C:\\SST Datasets\\NewQuerySet");

	@SuppressWarnings("unchecked")
	public static void sentenceBuilder() throws IOException {
		Queue<Context> contextList = Lists.newLinkedList();
		try {
			contextList = (Queue<Context>) ContextReader.GetContexts(FOLDERPATH);
		} catch (IOException e) {
			e.printStackTrace();
		}

		HistoryExtractor historyExtractor = new HistoryExtractor();
		while (!contextList.isEmpty()) {
			Context context = contextList.poll();
			try (FileWriter fw = new FileWriter(FOLDERPATH + "\\train_all", true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)) {
				try {
					Set<ConcreteHistory> extractedHistories = historyExtractor.extractHistories(context);
					for (ConcreteHistory concreteHistory : extractedHistories) {
						out.print(historyExtractor.getHistoryAsString(concreteHistory));
					}
				} catch (Exception e) {
					continue;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static void printContexts() throws IOException {
		List<Context> contextList = readContexts(FOLDERPATH);

		for (Context context : contextList) {
			String javaCode = new JavaPrinter().print(context);
			File file = JavaClassPathGenerator.generateClassPath(context.getSST(), FOLDERPATH.toString());
			writeStringToFile(file, javaCode);
		}
	}

	public static void queryBuilderWithRandomHoles() {
		queryBuilder(QueryStrategy.RANDOM);
	}

	public static void queryBuilderFromCompletionExpressions() {
		queryBuilder(QueryStrategy.COMPLETION);
	}

	private static void queryBuilder(QueryStrategy queryStrategy) {
		List<Context> contextList = readContexts(QUERY_FOLDER_PATH);

		QueryGenerator queryGenerator = new QueryGenerator(QUERY_FOLDER_PATH);
		for (Context context : contextList) {
			try {
				queryGenerator.generateQuery(context, queryStrategy);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		writeClassPaths(Sets.newHashSet(contextList));
	}

	private static List<Context> readContexts(Path path) {
		List<Context> contextList = new LinkedList<>();
		try {
			contextList = ContextReader.GetContexts(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contextList;
	}

	public static void queryBuilderFromCompletionEvents() {
		List<CompletionEvent> completionEventList = Lists.newLinkedList();
		try {
			completionEventList = ContextReader.GetCompletionEvents(QUERY_FOLDER_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}

		QueryGenerator queryGenerator = new QueryGenerator(QUERY_FOLDER_PATH);
		Set<Context> contexts = Sets.newHashSet();
		for (CompletionEvent completionEvent: completionEventList) {
			try {
				queryGenerator.generateQuery(completionEvent);
				contexts.add(completionEvent.getContext());
			} catch (Exception e) {
				continue;
			}
		}
		writeClassPaths(contexts);
	}

	private static void writeClassPaths(Set<Context> contexts) {
		JavaClassPathGenerator classPathGenerator = new JavaClassPathGenerator(QUERY_FOLDER_PATH.toString()); 
		try {
			classPathGenerator.generate(contexts);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		// sentenceBuilder();
		// printContexts();
		// queryBuilderFromCompletionExpressions();
		// queryBuilderFromCompletionEvents();
		queryBuilderWithRandomHoles();
	}

}