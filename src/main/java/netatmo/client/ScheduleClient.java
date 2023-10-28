package netatmo.client;

public class ScheduleClient extends AbstractApiClient {
	public ScheduleClient(AuthClient auth) {
		super();
		homeClient = new HomeClient(auth);
	}

	public java.util.List getSchedules(java.lang.String homeId) {
		return invokeMethod("getSchedules", new java.lang.Object[] { homeClient.invokeMethod("getHome", new java.lang.Object[] { homeId }) });
	}

	public static java.util.List getSchedules(java.util.Map homeData) {
		return ((java.util.List) (homeData.schedules));
	}

	public java.util.Map getSchedule(java.lang.String homeId, java.lang.String scheduleId) {
		return invokeMethod("getSchedule", new java.lang.Object[] { homeClient.invokeMethod("getHome", new java.lang.Object[] { homeId }), scheduleId });
	}

	public static java.util.Map getSchedule(java.util.Map homeData, java.lang.String scheduleId) {
		return getSchedule(getSchedules(homeData), scheduleId);
	}

	public static java.util.Map getSchedule(java.util.List schedules, final java.lang.String scheduleId) {
		return ((java.util.Map) (schedules.invokeMethod("find", new java.lang.Object[] { new Closure(this, this) {
			public java.lang.Boolean doCall(java.lang.Object it) {return it.id.equals(scheduleId);}

			public java.lang.Boolean doCall() {
				return doCall(null);
			}

		} })));
	}

	public java.util.Map getScheduleByName(java.lang.String homeId, java.lang.String name) {
		return invokeMethod("getScheduleByName", new java.lang.Object[] { homeClient.invokeMethod("getHome", new java.lang.Object[] { homeId }), name });
	}

	public static java.util.Map getScheduleByName(java.util.Map homeData, java.lang.String name) {
		return getScheduleByName(getSchedules(homeData), name);
	}

	public static java.util.Map getScheduleByName(java.util.List schedues, final java.lang.String name) {
		return ((java.util.Map) (schedues.invokeMethod("find", new java.lang.Object[] { new Closure(this, this) {
			public java.lang.Boolean doCall(java.lang.Object it) {return it.name.equals(name);}

			public java.lang.Boolean doCall() {
				return doCall(null);
			}

		} })));
	}

	public java.lang.Object listSchedules(java.lang.String homeId) {
		return invokeMethod("listSchedules", new java.lang.Object[] { homeClient.invokeMethod("getHome", new java.lang.Object[] { homeId }) });
	}

	public static java.lang.Object listSchedules(java.util.Map homeData) {
		return listSchedules(getSchedules(homeData));
	}

	public static java.lang.Object listSchedules(java.util.List schedules) {
		return schedules.invokeMethod("collect", new java.lang.Object[] { new Closure(this, this) {
			public java.util.LinkedHashMap<java.lang.String, java.lang.Object> doCall(java.lang.Object it) {
				java.util.LinkedHashMap<java.lang.String, java.lang.Object> map = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>(3);
				map.put("id", it.id);
				map.put("name", it.name);
				map.put("type", it.type);
				return map;
			}

			public java.util.LinkedHashMap<java.lang.String, java.lang.Object> doCall() {
				return doCall(null);
			}

		} });
	}

	public java.lang.Object setSchedule(java.lang.Object schedule, java.lang.String homeId) {
		if (!schedule.invokeMethod("containsKey", new java.lang.Object[] { SCHEDULE_ID }).asBoolean()) {
			schedule.invokeMethod("put", new java.lang.Object[] { SCHEDULE_ID, schedule.id });
		}

		if (!schedule.invokeMethod("containsKey", new java.lang.Object[] { HOME_ID }).asBoolean()) {
			assert homeId;
			schedule.invokeMethod("put", new java.lang.Object[] { HOME_ID, homeId });
		}

		schedule.invokeMethod("remove", new java.lang.Object[] { "id" });
		schedule.invokeMethod("remove", new java.lang.Object[] { "default" });
		schedule.invokeMethod("remove", new java.lang.Object[] { "type" });
		schedule.zones.invokeMethod("each", new java.lang.Object[] { new Closure(this, this) {
			public java.lang.Object doCall(java.lang.Object it) {return it.invokeMethod("remove", new java.lang.Object[] { "rooms_temp" });}

			public java.lang.Object doCall() {
				return doCall(null);
			}

		} });
		return invokeMethod("post",
			new java.lang.Object[] { "synchomeschedule", null, new JsonBuilder(schedule).invokeMethod("toString", new java.lang.Object[0]),
				"application/json" });
	}

	public java.lang.Object setSchedule(java.lang.Object schedule) {
		return setSchedule(schedule, null);
	}

	public static final java.lang.String SCHEDULE_ID = "schedule_id";
	public static final java.lang.String HOME_ID = "home_id";
	private final HomeClient homeClient;
}
