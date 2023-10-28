package net.suteren.netatmo.domain.therm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Module(
	String id,
	String name,
	String type,
	@JsonProperty("setup_date") Long setupDate,
	@JsonProperty("modules_bridged") List<String> modulesBridged,
	@JsonProperty("room_id") Long roomId,
	String bridge
) {}
