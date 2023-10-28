package net.suteren.netatmo.domain.therm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record TimetableEntry(
	@JsonProperty("zone_id") Integer zoneId,
	@JsonProperty("m_offset") int mOffset
) {}
