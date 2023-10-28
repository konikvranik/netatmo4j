package net.suteren.netatmo.domain.therm;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record Schedule(
	@JsonProperty("id") String id,
	@JsonProperty("home_id") String homeId,
	@JsonProperty("schedule_id") String scheduleId,
	String name,
	List<Zone> zones,
	List<TimetableEntry> timetable,
	@JsonProperty("hg_temp") Integer hgTemp,
	@JsonProperty("away_temp") Integer awayTemp,
	@JsonProperty("default") Boolean isDefaultZone,
	Boolean selected,
	String type
) {

	public String getId() {
		return Optional.ofNullable(id).orElse(scheduleId);
	}
}
