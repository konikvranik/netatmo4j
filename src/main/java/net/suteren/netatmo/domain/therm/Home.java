package net.suteren.netatmo.domain.therm;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.suteren.netatmo.PairDeserializer;
import net.suteren.netatmo.PairSerializer;

public record Home(
	String id,
	String name,
	Integer altitude,
	@JsonDeserialize(using = PairDeserializer.class)
	@JsonSerialize(using = PairSerializer.class)
	Pair<Double, Double> coordinates,
	String country,
	String timezone,
	List<Room> rooms,
	List<Module> modules,
	@JsonProperty("therm_set_point_default_duration") Integer thermSetPointDefaultDuration,
	@JsonProperty("therm_boost_default_duration") Integer thermBoostDefaultDuration,
	@JsonProperty("temperature_control_mode") String temperatureControlMode,
	@JsonProperty("therm_mode") String thermMode,
	@JsonProperty("therm_setpoint_default_duration") Integer thermSetpointDefaultDuration,
	List<Schedule> schedules
) {}
