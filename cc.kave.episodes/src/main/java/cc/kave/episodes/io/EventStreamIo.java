/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package cc.kave.episodes.io;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import cc.kave.commons.utils.json.JsonUtils;
import cc.kave.episodes.model.EventStream;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.kave.episodes.model.events.Fact;
import cc.recommenders.assertions.Asserts;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

public class EventStreamIo {

	public static final double DELTA = 0.001;
	public static final double TIMEOUT = 0.5;

	public static void write(EventStream stream, String fileStream,
			String fileMapping, String fileMethods) {
		try {
			FileUtils.writeStringToFile(new File(fileStream),
					stream.getStream());
			JsonUtils.toJson(stream.getMapping().keySet(),
					new File(fileMapping));
			JsonUtils.toJson(stream.getEnclMethods(), new File(fileMethods));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<Event> createMapping(List<Event> stream) {
		List<Event> mapping = Lists.newLinkedList();
		for (Event e : stream) {
			if (mapping.indexOf(e) == -1) {
				mapping.add(e);
			}
		}
		return mapping;
	}

	private static String toString(List<Event> stream, List<Event> mapping) {
		StringBuilder sb = new StringBuilder();

		boolean isFirstMethod = true;
		double time = 0.000;

		for (Event e : stream) {
			int idx = mapping.indexOf(e);
			Asserts.assertNotNegative(idx);

			if (e.getKind() == EventKind.METHOD_DECLARATION && !isFirstMethod) {
				time += TIMEOUT;
			}
			isFirstMethod = false;

			sb.append(idx);
			sb.append(',');
			sb.append(String.format("%.3f", time));
			sb.append('\n');

			time += DELTA;
		}
		return sb.toString();

	}

	public static String readStream(String path) throws IOException {
		String stream = FileUtils.readFileToString(new File(path));
		return stream;
	}

	public static List<List<Fact>> parseStream(String path) {
		List<List<Fact>> stream = Lists.newLinkedList();
		List<Fact> method = Lists.newLinkedList();

		List<String> lines = readlines(path);

		double timer = -1;

		for (String line : lines) {
			String[] eventTime = line.split(",");
			int eventID = Integer.parseInt(eventTime[0]);
			double timestamp = Double.parseDouble(eventTime[1]);
			if (timer != -1) {
				while ((timestamp - timer) > TIMEOUT) {
					stream.add(method);
					method = new LinkedList<Fact>();
					timer += TIMEOUT;
				}
			}
			timer = timestamp;
			method.add(new Fact(eventID));
		}
		stream.add(method);
		return stream;
	}

	public static List<Event> readMethods(String path) {
		@SuppressWarnings("serial")
		Type type = new TypeToken<List<Event>>() {
		}.getType();
		return JsonUtils.fromJson(new File(path), type);
	}

	public static List<Event> readMapping(String path) {
		@SuppressWarnings("serial")
		Type type = new TypeToken<List<Event>>() {
		}.getType();
		return JsonUtils.fromJson(new File(path), type);
	}

	private static List<String> readlines(String path) {
		List<String> lines = new LinkedList<String>();

		try {
			lines = FileUtils.readLines(new File(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return lines;
	}
}