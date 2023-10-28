package net.suteren.netatmo.client;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public record NetatmoResponse(
	String status,
	@JsonProperty("time_exec") Float timeExec,
	@JsonProperty("time_server") Long timeServer,
	JsonNode body
) {

	public Instant getServerTime() {
		return Instant.ofEpochSecond(timeServer);
	}
}
