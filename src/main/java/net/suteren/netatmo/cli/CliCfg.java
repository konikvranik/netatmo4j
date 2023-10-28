package net.suteren.netatmo.cli;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.suteren.netatmo.PresenceMode;

@JsonSerialize
public record CliCfg(
	@JsonProperty("home_id")
	String homeId,
	TemperatureCfg temperatures,
	Map<String, Map<String, PresenceMode>> modes
) {}
