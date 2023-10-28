package net.suteren.netatmo.therm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import net.suteren.netatmo.auth.AuthClient;
import net.suteren.netatmo.client.AbstractApiClient;
import net.suteren.netatmo.client.ConnectionException;
import net.suteren.netatmo.client.NetatmoResponse;
import net.suteren.netatmo.domain.therm.Home;
import net.suteren.netatmo.domain.therm.HomesData;
import net.suteren.netatmo.domain.therm.Room;

public class HomeClient extends AbstractApiClient<InputStream> {
	public HomeClient(AuthClient auth) {
		super(auth);
	}

	public HomesData getHomesData() throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return OBJECT_MAPPER.treeToValue(OBJECT_MAPPER.readValue(get("homesdata"), NetatmoResponse.class).body(), HomesData.class);
	}

	public List<Home> getHomes() throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getHomesData().homes();
	}

	public Home getHome(final String homeId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getHomes().stream()
			.filter(h -> Objects.equals(h.id(), homeId))
			.findAny()
			.orElse(null);
	}

	public JsonNode getStatus(java.lang.String homeId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return OBJECT_MAPPER.readTree(get("homestatus", Map.of("home_id", homeId)));
	}

	public List<Room> getRooms(String homeId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getRooms(getHome(homeId));
	}

	public static List<Room> getRooms(Home homeData) {
		return homeData.rooms().stream()
			.toList();
	}

	public Room getRoom(String homeId, final String roomId)
		throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getRoom(getHome(homeId), roomId);
	}

	public static Room getRoom(Home homeData, String roomId) {
		return getRoom(getRooms(homeData), roomId);

	}

	public static Room getRoom(List<Room> rooms, final String roomId) {
		return rooms.stream()
			.filter(r -> Objects.equals(r.id(), roomId))
			.findAny()
			.orElse(null);
	}

	public Room getRoomByName(String homeId, final String roomId)
		throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getRoomByName(getHome(homeId), roomId);
	}

	public static Room getRoomByName(Home homeData, String roomId) {
		return getRoomByName(getRooms(homeData), roomId);

	}

	public static Room getRoomByName(List<Room> rooms, final String roomId) {
		return rooms.stream()
			.filter(r -> Objects.equals(r.name(), roomId))
			.findAny()
			.orElse(null);
	}

}
