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
package exec.examples;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

import cc.kave.commons.model.events.completionevents.Context;
import cc.recommenders.io.IReadingArchive;
import cc.recommenders.io.ReadingArchive;

/**
 * this class explains how contexts can be read from the file system
 */
public class IoHelper {

	public static Context readFirstContext(String dir) {
		for (String zip : findAllZips(dir)) {
			List<Context> ctxs = read(zip);
			return ctxs.get(0);
		}
		return null;
	}

	public static List<Context> readAll(String dir) {
		LinkedList<Context> res = Lists.newLinkedList();

		for (String zip : findAllZips(dir)) {
			res.addAll(read(zip));
		}
		return res;
	}

	public static List<Context> read(String zipFile) {
		LinkedList<Context> res = Lists.newLinkedList();
		try {
			IReadingArchive ra = new ReadingArchive(new File(zipFile));
			while (ra.hasNext()) {
				res.add(ra.getNext(Context.class));
			}
			ra.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public static List<String> findAllZips(String dir) {
		List<String> zips = Lists.newLinkedList();
		for (File f : FileUtils.listFiles(new File(dir), new String[] { "zip" }, true)) {
			zips.add(f.getAbsolutePath());
		}
		return zips;
	}
}