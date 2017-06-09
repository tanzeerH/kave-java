package cc.kave.episodes.preprocessing;

import java.util.List;

import cc.kave.episodes.data.ContextsParser;
import cc.kave.episodes.eventstream.StreamFileGenerator;
import cc.kave.episodes.io.EventStreamIo;
import cc.kave.episodes.model.EventStream;
import cc.kave.episodes.model.events.Event;
import cc.recommenders.datastructures.Tuple;
import cc.recommenders.io.Logger;

import com.google.inject.Inject;

public class Preprocessing {

	private ContextsParser ctxParser;
	private EventStreamIo eventStreamIo;

	@Inject
	public Preprocessing(ContextsParser repositories, EventStreamIo streamData) {
		this.ctxParser = repositories;
		this.eventStreamIo = streamData;
	}

	public void run(int frequency) throws Exception {

		List<Tuple<Event, List<Event>>> eventStream = ctxParser.parse();

		Logger.log("Generating event stream data for freq = %d ...", frequency);
		EventStream stream = StreamFileGenerator.generate(eventStream,
				frequency);
		eventStreamIo.write(stream, frequency);
	}
}
