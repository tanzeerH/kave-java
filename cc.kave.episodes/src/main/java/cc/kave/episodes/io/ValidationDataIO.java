package cc.kave.episodes.io;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.reflect.TypeToken;

import cc.kave.commons.utils.json.JsonUtils;
import cc.kave.episodes.model.events.Event;

public class ValidationDataIO {

	private File repoDir;
	
	@Inject
	public ValidationDataIO(@Named("repositories") File folder) {
		assertTrue(folder.exists(), "Repositories folder does not exist");
		assertTrue(folder.isDirectory(),
				"Repositories is not a folder, but a file");
		this.repoDir = folder;
	}
	
	public void write(List<Event> stream, int fold) {
		JsonUtils.toJson(stream, getValidationPath(fold));
	}

	private File getValidationPath(int foldNum) {
		File path = new File(repoDir.getAbsolutePath() + "/ValidationData");
		if (!path.exists()) {
			path.mkdir();
		}
		File fileName = new File(path.getAbsoluteFile() + "/stream" + foldNum + ".json");
		return fileName;
	}
	
	public List<Event> read(int foldNum) {
		@SuppressWarnings("serial")
		Type type = new TypeToken<List<Event>>() {
		}.getType();
		List<Event> stream = JsonUtils.fromJson(getValidationPath(foldNum), type);
		return stream;
	}
}
