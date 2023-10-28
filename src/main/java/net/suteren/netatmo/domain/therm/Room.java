package net.suteren.netatmo.domain.therm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record Room(
	String id,
	@JsonProperty("therm_setpoint_temperature") int thermSetpointTemperature
) {}
