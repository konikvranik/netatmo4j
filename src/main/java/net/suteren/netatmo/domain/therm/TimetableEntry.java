package net.suteren.netatmo.domain.therm;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record TimetableEntry(
	@JsonProperty("zone_id") Integer zoneId,
	@JsonProperty("m_offset") int mOffset
) {
	@JsonIgnore public static final int MINUTES_PER_DAY = 1440;

	@JsonIgnore public DayOfWeek getDayOfWeek() {
		return DayOfWeek.of(mOffset / MINUTES_PER_DAY + 1);
	}

	@JsonIgnore public int getDayOffset() {
		return mOffset % MINUTES_PER_DAY;
	}

	@JsonIgnore public LocalTime getTimeOfDay() {
		return LocalTime.of(getDayOffset() / 60, getDayOffset() % 60);
	}
}
