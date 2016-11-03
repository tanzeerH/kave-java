package cc.kave.episodes.io;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import javax.inject.Inject;
import javax.inject.Named;

import cc.kave.commons.utils.json.JsonUtils;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RepoMethodsMapperIO {

	private File reposDir;
	
	@Inject
	public RepoMethodsMapperIO(@Named("repositories") File folder) {
		assertTrue(folder.exists(), "Repositories folder does not exist");
		assertTrue(folder.isDirectory(),
				"Repositories is not a folder, but a file");
		this.reposDir = folder;
	}
	
	public void writer(Map<String, List<Event>> repos) throws ZipException, IOException {
		Map<String, List<Event>> repoMethods = Maps.newLinkedHashMap();

		for (Map.Entry<String, List<Event>> entry : repos.entrySet()) {
			List<Event> methods = Lists.newLinkedList();
			List<Event> events = entry.getValue();

			for (Event e : events) {
				if (e.getKind() == EventKind.METHOD_DECLARATION) {
					methods.add(e);
				}
			}
			repoMethods.put(entry.getKey(), methods);
			JsonUtils.toJson(repoMethods, new File(repoMethodsPath()));
		}
	}
	
	private String repoMethodsPath() {
		String fileName = reposDir.getAbsolutePath() + "/repoMethodsMapper.json";
		return fileName;
	}
}