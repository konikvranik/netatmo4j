package net.suteren.netatmo;

import java.util.Map;
import java.util.Optional;

import net.suteren.netatmo.cli.CliCfg;
import net.suteren.netatmo.cli.RoomCfg;

import static net.suteren.netatmo.PresenceMode.AWAY;
import static net.suteren.netatmo.PresenceMode.REGULAR;
import static net.suteren.netatmo.PresenceMode.WORK;

public class ZonePresets {
	Map<String, Map<String, PresenceMode>> modes;
	private final Map<String, RoomCfg> rooms;
	private final int day;
	private final int night;
	private final int away;

	public ZonePresets(CliCfg cfg) {
		rooms = cfg.temperatures().rooms();
		day = cfg.temperatures().day();
		night = cfg.temperatures().night();
		away = cfg.temperatures().away();
		modes = cfg.modes();
	}

	public int getTemp(String scheduleId, int zoneId, String roomId) {

		PresenceMode mode = Optional.ofNullable(modes).map(m -> m.get(scheduleId)).map(s -> s.get(roomId)).orElse(REGULAR);
		if (mode == AWAY || (mode == WORK && zoneId == 0)) {
			return getAway(roomId);
		} else {
			return switch (zoneId) {
				case 0 -> getDay(roomId); // Komfortní
				case 1 -> getNight(roomId); // Noc
				case 3 -> getDay(roomId); // Komfortní+
				case 4 -> getAway(roomId); // Úsporný
				default -> throw new IllegalStateException("Unexpected value: " + zoneId);
			};
		}
	}

	private int getDay(String roomId) {
		return Optional.ofNullable(rooms.get(roomId))
			.map(RoomCfg::day)
			.orElse(day);
	}

	private int getNight(String roomId) {
		return Optional.ofNullable(rooms.get(roomId))
			.map(RoomCfg::night)
			.orElse(night);
	}

	private int getAway(String roomId) {
		return Optional.ofNullable(rooms.get(roomId))
			.map(RoomCfg::away)
			.orElse(away);
	}
}
