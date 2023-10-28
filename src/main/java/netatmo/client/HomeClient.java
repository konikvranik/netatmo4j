package netatmo.client;

public class HomeClient extends AbstractApiClient {
	public HomeClient(AuthClient auth) {
		super();
	}

	public java.util.Map getConfig() {
		java.util.Map object = invokeMethod("get", new java.lang.Object[] { "homesdata" });
		object.body.invokeMethod("remove", new java.lang.Object[] { "homes" });
		return object;
	}

	public java.util.List getHomes() {
		return ((java.util.List) (invokeMethod("get", new java.lang.Object[] { "homesdata" }).body.homes));
	}

	public java.util.List listHomes() {
		java.util.List data = getHomes();
		data.invokeMethod("each", new java.lang.Object[] { new Closure(this, this) {
			public java.lang.Object doCall(java.lang.Object it) {
				it.invokeMethod("remove", new java.lang.Object[] { "schedules" });
				it.invokeMethod("remove", new java.lang.Object[] { "rooms" });
				return it.invokeMethod("remove", new java.lang.Object[] { "modules" });
			}

			public java.lang.Object doCall() {
				return doCall(null);
			}

		} });
		//.collect { [id: it.id, name: it.name] }
		return ((java.util.List) (data));
	}

	public java.util.Map getHome(final java.lang.String homeId) {
		java.util.LinkedHashMap<java.lang.String, java.lang.String> map = new java.util.LinkedHashMap<java.lang.String, java.lang.String>(1);
		map.put("home_id", homeId);
		return ((java.util.Map) (invokeMethod("get", new java.lang.Object[] { "homesdata", map }).body.homes.invokeMethod("find",
			new java.lang.Object[] { new Closure(this, this) {
				public java.lang.Boolean doCall(java.lang.Object it) {return it.id.equals(homeId);}

				public java.lang.Boolean doCall() {
					return doCall(null);
				}

			} })));
	}

	public java.lang.Object getStatus(java.lang.String homeId) {
		java.util.LinkedHashMap<java.lang.String, java.lang.String> map = new java.util.LinkedHashMap<java.lang.String, java.lang.String>(1);
		map.put("home_id", homeId);
		return invokeMethod("get", new java.lang.Object[] { "homestatus", map });
	}

	public java.util.List getRooms(java.lang.String homeId) {
		return ((java.util.List) (getHome(homeId).rooms));
	}

	public static java.util.List getRooms(java.util.Map homeData) {
		return ((java.util.List) (homeData.rooms));
	}

	public java.util.Map getRoom(java.lang.String homeId, final java.lang.String roomId) {
		return ((java.util.Map) (getRooms(homeId).invokeMethod("find", new java.lang.Object[] { new Closure(this, this) {
			public java.lang.Boolean doCall(java.lang.Object it) {return it.id.equals(roomId);}

			public java.lang.Boolean doCall() {
				return doCall(null);
			}

		} })));
	}

	public static java.util.Map getRoom(java.util.Map homeData, java.lang.String roomId) {
		return getRoom(getRooms(homeData), roomId);
	}

	public static java.util.Map getRoom(java.util.List rooms, final java.lang.String roomId) {
		return ((java.util.Map) (rooms.invokeMethod("find", new java.lang.Object[] { new Closure(this, this) {
			public java.lang.Boolean doCall(java.lang.Object it) {return it.id.equals(roomId);}

			public java.lang.Boolean doCall() {
				return doCall(null);
			}

		} })));
	}

	public java.util.Map getRoomByName(java.lang.String homeId, final java.lang.String name) {
		return ((java.util.Map) (getRooms(homeId).invokeMethod("find", new java.lang.Object[] { new Closure(this, this) {
			public java.lang.Boolean doCall(java.lang.Object it) {return it.name.equals(name);}

			public java.lang.Boolean doCall() {
				return doCall(null);
			}

		} })));
	}

	public static java.util.Map getRoomByName(java.util.List rooms, final java.lang.String name) {
		return ((java.util.Map) (rooms.invokeMethod("find", new java.lang.Object[] { new Closure(this, this) {
			public java.lang.Boolean doCall(java.lang.Object it) {return it.name.equals(name);}

			public java.lang.Boolean doCall() {
				return doCall(null);
			}

		} })));
	}

	public static java.util.Map getRoomByName(java.util.Map homeData, java.lang.String name) {
		return getRoomByName(getRooms(homeData), name);
	}

}
