package cc.kave.episodes.mining.evaluation;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.kave.episodes.io.EventStreamIo;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.kave.episodes.model.events.Fact;
import cc.recommenders.io.Logger;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class MethodSize {

	private File directory;
	private EventStreamIo eventStreamIo;

	@Inject
	public MethodSize(@Named("events") File folder) {
		assertTrue(folder.exists(), "Events folder does not exist");
		assertTrue(folder.isDirectory(), "Events is not a folder, but a file");
		this.directory = folder;
	}

	public void statistics(int numbRepos, int methodLength) {
		List<List<Fact>> stream = eventStreamIo.parseStream(getStreamPath(numbRepos));
		int streamLength = calcStreamLength(stream);
		
		List<Event> events = eventStreamIo.readMapping(getMappingPath(numbRepos));

		List<Event> methods = eventStreamIo.readMethods(getMethodsPath(numbRepos));
		Set<Event> uniqMethods = listToSet(methods);
		assertTrue(uniqMethods.size() <= methods.size(), "Error in converting List to Set!");

		Logger.log("Number of methods in stream data is %d", stream.size());
		Logger.log("Number of events in the event stream is %d", streamLength);
		Logger.log("Number of unique events is %d", events.size());
		Logger.log("Number of enclosing methods is %d", uniqMethods.size());

//		checkMethodSize(stream, events, methodLength);
	}

	private int calcStreamLength(List<List<Fact>> stream) {
		int length = 0;
		
		for (List<Fact> method : stream) {
			length += method.size();
		}
		return length;
	}

	private Set<Event> listToSet(List<Event> methods) {
		Set<Event> result = Sets.newLinkedHashSet();
		
		for (Event m : methods) {
			result.add(m);
		}
		return result;
	}

	private void checkMethodSize(List<List<Fact>> stream, List<Event> events,
			int methodLength) {
		Map<List<Fact>, Integer> methods = Maps.newLinkedHashMap();
		int longMethodSize = 0;
		List<Fact> longestMethod = new LinkedList<Fact>();
		for (List<Fact> method : stream) {
			if (method.size() >= methodLength) {
				methods.put(method, method.size());
			}
			if (method.size() > longMethodSize) {
				longestMethod.clear();
				longestMethod.addAll(method);
				longMethodSize = method.size();
			}
		}
		printLongestMethod(longestMethod, events, longMethodSize);
		printMethodsAboveThreshold(methods, events, methodLength);
	}

	private void printMethodsAboveThreshold(Map<List<Fact>, Integer> methods,
			List<Event> events, int methodLength) {
		if (!methods.isEmpty()) {
			Logger.log("Methods with more than %d events are: %d",
					methodLength, methods.size());
			for (Map.Entry<List<Fact>, Integer> m : methods.entrySet()) {
				String methodName = getMethodName(m.getKey(), events);
				Logger.log("Method %s\thas %d events", methodName, m.getValue());
			}
		}
	}

	private void printLongestMethod(List<Fact> longestMethod,
			List<Event> events, int longMethodSize) {
		String methodName = getMethodName(longestMethod, events);
		Logger.log("Size of the largest method is: %d", longMethodSize);
		Logger.log("The longest method is: %s", methodName);

	}

	private String getMethodName(List<Fact> method, List<Event> events) {
		String fileName = "";
		for (Fact fact : method) {
			Event event = events.get(fact.getFactID());
			if (event.getKind() == EventKind.METHOD_DECLARATION) {
				fileName = event.getMethod().getDeclaringType().getFullName()
						+ "." + event.getMethod().getName();
				// fileName = event.getMethod().getIdentifier();
				break;
			}
		}
		return fileName;
	}

	private File getPath(int numRepos) {
		File path = new File(directory.getAbsolutePath() + "/" + numRepos
				+ "Repos");
		return path;
	}
	
	private String getStreamPath(int numRepos) {
		String fileName = getPath(numRepos).getAbsolutePath() + "/stream.txt";
		return fileName;
	}
	
	private String getMappingPath(int numRepos) {
		String fileName = getPath(numRepos).getAbsolutePath() + "/mapping.txt";
		return fileName;
	}
	
	private String getMethodsPath(int numRepos) {
		String fileName = getPath(numRepos).getAbsolutePath() + "/methods.txt";
		return fileName;
	}
}
