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
package cc.kave.commons.pointsto.evaluation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import cc.kave.commons.pointsto.io.IOHelper;
import cc.recommenders.names.ICoReTypeName;
import cc.recommenders.names.CoReNames;

public class CSVExporter implements ResultExporter {

	private static final String SEPARATOR = " ";

	@Override
	public void export(Path target, Map<ICoReTypeName, Double> results) throws IOException {
		List<ICoReTypeName> types = new ArrayList<>(results.keySet());
		types.sort(new TypeNameComparator());

		IOHelper.createParentDirs(target);
		try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
			for (ICoReTypeName type : types) {
				String typeName = CoReNames.vm2srcQualifiedType(type);
				writer.append(typeName);
				writer.append(SEPARATOR);
				writer.append(String.format(Locale.US, "%.3f", results.get(type)));
				writer.newLine();
			}
		}
	}

	@Override
	public void export(Path target, Stream<String[]> lines) throws IOException {
		Stream<String> sortedLines = lines.map(l -> String.join(SEPARATOR, l)).sorted();
		Files.write(target, (Iterable<String>) sortedLines::iterator, StandardCharsets.UTF_8);
	}

}
