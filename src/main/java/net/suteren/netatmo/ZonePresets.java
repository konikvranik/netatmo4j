package net.suteren.netatmo

class ZonePresets {
	Map<String, Map<String, PresenceMode>> modes
	private final Map<String, Map<String, Integer>> rooms
	private final int day
	private final int night
	private final int away


	ZonePresets(Map cfg) {
		rooms = cfg.temperatures.rooms
		day = cfg.temperatures.day
		night = cfg.temperatures.night
		away = cfg.temperatures.away
		modes = cfg.modes.collectEntries { k, v -> [(k): v.collectEntries { k1, v1 -> [(k1): valueOf(v1?.toUpperCase() ?: "REGULAR")] }] }
	}

	int getTemp(String scheduleId, int zoneId, String roomId) {
		PresenceMode mode = modes.get(scheduleId)?.get(roomId) ?: REGULAR

		if (mode == AWAY || (mode == WORK && zoneId == 0)) {
			return getAway(roomId)
		} else {
			switch (zoneId) {
				case 0: // Komfortní
					return getDay(roomId)
				case 1: // Noc
					return getNight(roomId)
				case 3: // Komfortní+
					return getDay(roomId)
				case 4: // Úsporný
					return getAway(roomId)
			}
		}
	}


	private int getDay(String roomId) {
		rooms.get(roomId)?.day ?: day
	}

	private int getNight(String roomId) {
		rooms.get(roomId)?.night ?: night
	}

	private int getAway(String roomId) {
		rooms.get(roomId)?.away ?: away
	}
}
