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
package exec.recommender_reimplementation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cc.kave.commons.model.events.completionevents.Context;

public class ContextReaderTest {
	
	private static Path FolderPath = Paths.get("C:\\SST Datasets\\NewQueryset");
	
	@Test
	public void ContextListNotEmpty() throws IOException {
		List<Context> contextList = ContextReader.GetContexts(FolderPath);
		Assert.assertTrue(!contextList.isEmpty());
	}
}