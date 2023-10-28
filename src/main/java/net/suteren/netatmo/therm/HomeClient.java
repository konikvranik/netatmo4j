package net.suteren.netatmo.therm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.suteren.netatmo.auth.AuthClient;
import net.suteren.netatmo.client.AbstractApiClient;
import net.suteren.netatmo.client.ConnectionException;

public class HomeClient extends AbstractApiClient<InputStream> {
	public HomeClient(AuthClient auth) {
		super(auth);
	}

	public ObjectNode getConfig() throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		ObjectNode object = (ObjectNode) OBJECT_MAPPER.readTree(get("homesdata"));
		((ObjectNode) object.at("/body")).remove("homes");
		return object;
	}

	public List<ObjectNode> getHomes() throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return IterableUtils.toList(OBJECT_MAPPER.readTree(get("homesdata")).at("/body/homes")).stream()
			.map(ObjectNode.class::cast)
			.collect(Collectors.toList());
	}

	public List<ObjectNode> listHomes() throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getHomes().stream()
			.peek(n -> {
					n.remove("schedules");
					n.remove("rooms");
					n.remove("modules");
				}
			)
			.toList();
	}

	public ObjectNode getHome(final String homeId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getHomes().stream()
			.filter(h -> Objects.equals(h.findValue("id").textValue(), homeId))
			.findAny()
			.orElse(null);
	}

	private void test(ObjectNode h) {

	}

	public JsonNode getStatus(java.lang.String homeId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return OBJECT_MAPPER.readTree(get("homestatus", Map.of("home_id", homeId)));
	}

	public List<ObjectNode> getRooms(java.lang.String homeId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getRooms(getHome(homeId));
	}

	public static List<ObjectNode> getRooms(ObjectNode homeData) {
		return IterableUtils.toList(homeData.at("/rooms")).stream()
			.map(ObjectNode.class::cast)
			.toList();
	}

	public ObjectNode getRoom(String homeId, final String roomId)
		throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getRoom(getHome(homeId), roomId);
	}

	public static ObjectNode getRoom(ObjectNode homeData, String roomId) {
		return getRoom(getRooms(homeData), roomId);

	}

	public static ObjectNode getRoom(List<ObjectNode> rooms, final String roomId) {
		return rooms.stream()
			.filter(r -> Objects.equals(r.at("/id").textValue(), roomId))
			.findAny()
			.orElse(null);
	}

	public ObjectNode getRoomByName(String homeId, final String roomId)
		throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getRoomByName(getHome(homeId), roomId);
	}

	public static ObjectNode getRoomByName(ObjectNode homeData, String roomId) {
		return getRoomByName(getRooms(homeData), roomId);

	}

	public static ObjectNode getRoomByName(List<ObjectNode> rooms, final String roomId) {
		return rooms.stream()
			.filter(r -> Objects.equals(r.at("/name").textValue(), roomId))
			.findAny()
			.orElse(null);
	}

}
