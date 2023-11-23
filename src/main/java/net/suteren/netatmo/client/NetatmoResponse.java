package net.suteren.netatmo.client;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Java representation of the generic Netatmo API response.
 *
 * @param status status of the request.
 * @param timeExec execution time of the request.
 * @param timeServer server time.
 * @param body content of the response.
 */
public record NetatmoResponse(
	String status,
	@JsonProperty("time_exec") Float timeExec,
	@JsonProperty("time_server") Long timeServer,
	JsonNode body
) {

	/**
	 * Get the server's time as an {@link Instant}.
	 *
	 * @return the server's time.
	 */
	public Instant getServerTime() {
		return Instant.ofEpochSecond(timeServer);
	}
}
