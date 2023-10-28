package net.suteren.netatmo.domain.therm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record Room(
	String id,
	String name,
	String type,
	@JsonProperty("module_ids") List<String> moduleIds,
	@JsonProperty("therm_setpoint_temperature") int thermSetpointTemperature
) {}
