package net.suteren.netatmo.domain;

import java.util.List;

record Schedule(
	String homeId,
	String scheduleId,
	String name,
	List<Zone> zones,
	List<TimetableEntry> timetable,
	int hgTemp,
	int awayTemp
) {}
