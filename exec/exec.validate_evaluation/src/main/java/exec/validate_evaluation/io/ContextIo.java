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
package exec.validate_evaluation.io;

import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;

import cc.kave.commons.model.events.completionevents.Context;
import cc.recommenders.io.Directory;
import cc.recommenders.io.IReadingArchive;
import cc.recommenders.io.WritingArchive;

public class ContextIo {

	private String root;

	public ContextIo(String root) {
		this.root = root;
	}

	public Set<String> findZips() {
		Directory dir = new Directory(this.root);
		return dir.findFiles(s -> s.endsWith(".zip"));
	}

	public Set<Context> read(String zip) {
		Set<Context> contexts = Sets.newLinkedHashSet();
		Directory dir = new Directory(this.root);

		try (IReadingArchive ra = dir.getReadingArchive(zip)) {
			while (ra.hasNext()) {
				contexts.add(ra.getNext(Context.class));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return contexts;
	}

	public void write(Set<Context> contexts, String zip) {
		Directory dir = new Directory(this.root);
		try (WritingArchive wa = dir.getWritingArchive(zip)) {
			for (Context mc : contexts) {
				wa.add(mc);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}