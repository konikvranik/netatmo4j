package net.suteren.netatmo;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class PairDeserializer extends StdDeserializer<Pair<Double, Double>> {

	protected PairDeserializer() {
		super(Pair.class);
	}

	@Override public Pair<Double, Double> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		Iterator<JsonNode> elements = jp.getCodec().<ArrayNode>readTree(jp).elements();
		return Pair.of(elements.next().doubleValue(), elements.next().doubleValue());
	}
}