package net.suteren.netatmo.domain.therm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record Zone(
	int id,
	String name,
	int type,
	@JsonProperty("rooms_temp") List<RoomTemp> roomstemp,
	List<Room> rooms,
	List<Module> modules
) {}
