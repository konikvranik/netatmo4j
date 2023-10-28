package net.suteren.netatmo.cli;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record RoomCfg(
	Integer day,
	Integer night,
	Integer away
) {}
