package net.suteren.netatmo.domain.therm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record User(
	String id,
	String email,
	String language,
	String locale,
	@JsonProperty("feel_like_algorithm") Integer feelLikeAlgorithm,
	@JsonProperty("unit_pressure") Integer unitPressure,
	@JsonProperty("unit_system") Integer unit_system,
	@JsonProperty("unit_wind") Integer unitWind

) {}
