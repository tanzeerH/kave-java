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
package cc.kave.episodes.export;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.reflect.TypeToken;

import cc.kave.commons.utils.json.JsonUtils;
import cc.kave.episodes.model.EventStream;
import cc.kave.episodes.model.events.Event;

public class EventStreamIo {
	
	public static final double DELTA = 0.001;
	public static final double TIMEOUT = 0.5;
	
	public static void write(EventStream stream, String fileStream, String fileMapping) {
		try {
			FileUtils.writeStringToFile(new File(fileStream), stream.getStream());
			JsonUtils.toJson(stream.getMapping().keySet(), new File(fileMapping));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<Event> readMapping(String path) {
		@SuppressWarnings("serial")
		Type type = new TypeToken<List<Event>>() {
		}.getType();
		return JsonUtils.fromJson(new File(path), type);
	}
}