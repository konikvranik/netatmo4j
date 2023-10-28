package net.suteren.netatmo.domain.therm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record RoomTemp(
	@JsonProperty("room_id") String roomId,
	int temp
) {}
