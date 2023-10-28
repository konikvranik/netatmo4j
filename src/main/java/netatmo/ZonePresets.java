package netatmo;

public class ZonePresets extends GroovyObjectSupport {
	public ZonePresets(java.util.Map cfg) {
		rooms = ((java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Integer>>) (cfg.temperatures.rooms));
		day = ((int) (cfg.temperatures.day));
		night = ((int) (cfg.temperatures.night));
		away = ((int) (cfg.temperatures.away));
		modes = ((java.util.Map<java.lang.String, java.util.Map<java.lang.String, PresenceMode>>) (cfg.modes.invokeMethod("collectEntries",
			new java.lang.Object[] { new Closure(this, this) {
				public java.util.LinkedHashMap doCall(java.lang.Object k, java.lang.Object v) {
					java.util.LinkedHashMap map = new java.util.LinkedHashMap(1);
					map.put(k, v.invokeMethod("collectEntries",
						new java.lang.Object[] { new Closure(DUMMY__1234567890_DUMMYYYYYY___.this, DUMMY__1234567890_DUMMYYYYYY___.this) {
							public java.util.LinkedHashMap doCall(java.lang.Object k1, java.lang.Object v1) {
								java.util.LinkedHashMap map1 = new java.util.LinkedHashMap(1);
								final java.lang.Object upperCase = v1.invokeMethod("toUpperCase", new java.lang.Object[0]);
								map1.put(k1, invokeMethod("valueOf", new java.lang.Object[] { upperCase ? upperCase : "REGULAR" }));
								return map1;
							}

						} }));
					return map;
				}

			} })));
	}

	public int getTemp(java.lang.String scheduleId, int zoneId, java.lang.String roomId) {
		final PresenceMode get = modes.get(scheduleId).get(roomId);
		PresenceMode mode = get.asBoolean() ? get : REGULAR;

		if (mode == AWAY || (mode == WORK && zoneId == 0)) {
			return getAway(roomId);
		} else {
			switch (zoneId) {
			case 0:
				return getDay(roomId);
			case 1:
				return getNight(roomId);
			case 3:
				return getDay(roomId);
			case 4:
				return getAway(roomId);
			}
		}

	}

	private int getDay(java.lang.String roomId) {
		final java.util.Map<java.lang.String, java.lang.Integer> get = rooms.get(roomId);
		final java.lang.Integer day = (get == null ? null : get.day);
		return day.asBoolean() ? day : day;
	}

	private int getNight(java.lang.String roomId) {
		final java.util.Map<java.lang.String, java.lang.Integer> get = rooms.get(roomId);
		final java.lang.Integer night = (get == null ? null : get.night);
		return night.asBoolean() ? night : night;
	}

	private int getAway(java.lang.String roomId) {
		final java.util.Map<java.lang.String, java.lang.Integer> get = rooms.get(roomId);
		final java.lang.Integer away = (get == null ? null : get.away);
		return away.asBoolean() ? away : away;
	}

	public java.util.Map<java.lang.String, java.util.Map<java.lang.String, PresenceMode>> getModes() {
		return modes;
	}

	public void setModes(java.util.Map<java.lang.String, java.util.Map<java.lang.String, PresenceMode>> modes) {
		this.modes = modes;
	}

	private java.util.Map<java.lang.String, java.util.Map<java.lang.String, PresenceMode>> modes;
	private final java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Integer>> rooms;
	private final int day;
	private final int night;
	private final int away;
}
