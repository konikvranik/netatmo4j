package net.suteren.netatmo;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class PairSerializer extends StdSerializer<Pair<Double, Double>> {

	protected PairSerializer() {
		super(TypeFactory.defaultInstance().constructParametricType(Pair.class, Double.class, Double.class));
	}

	@Override public void serialize(Pair<Double, Double> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeArray(new double[] { value.getKey(), value.getValue() }, 0, 2);
	}
}
