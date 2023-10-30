package net.suteren.netatmo;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PairDeserializerTest {

	private final static ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

	@Test void deserialize() throws JsonProcessingException {
		assertEquals(Pair.of(10.14, 20.23), OBJECT_MAPPER.readValue("[10.14,20.23]", Pair.class));
	}
}