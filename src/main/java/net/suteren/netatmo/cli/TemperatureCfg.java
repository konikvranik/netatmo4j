package net.suteren.netatmo.cli;

import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record TemperatureCfg(
	Integer day,
	Integer night,
	Integer away,
	Map<String, RoomCfg> rooms
) {}
