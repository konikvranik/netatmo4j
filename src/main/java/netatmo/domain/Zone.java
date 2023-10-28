package netatmo.domain;

import java.util.List;

record Zone(
	long id,
	String name,
	int type,
	List<RoomTemp> roomstemp,
	List<Room> rooms
) {}
