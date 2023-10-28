package net.suteren.netatmo.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.IterableUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.suteren.netatmo.auth.AuthClient;

public class ScheduleClient extends AbstractApiClient<InputStream> {

	public static final java.lang.String SCHEDULE_ID = "schedule_id";
	public static final java.lang.String HOME_ID = "home_id";
	private final HomeClient homeClient;

	public ScheduleClient(AuthClient auth) {
		super(auth);
		homeClient = new HomeClient(auth);
	}

	public List<ObjectNode> getSchedules(String homeId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getSchedules(homeClient.getHome(homeId));
	}

	public static List<ObjectNode> getSchedules(ObjectNode homeData) {
		return IterableUtils.toList(homeData.at("/schedules")).stream()
			.map(ObjectNode.class::cast)
			.toList();
	}

	public ObjectNode getSchedule(String homeId, String scheduleId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getSchedule(getSchedules(homeId), scheduleId);
	}

	public static ObjectNode getSchedule(ObjectNode homeData, String scheduleId) {
		return getSchedule(getSchedules(homeData), scheduleId);
	}

	public static ObjectNode getSchedule(List<ObjectNode> schedules, String scheduleId) {
		return schedules.stream()
			.filter(s -> Objects.equals(s.at("/id").textValue(), scheduleId))
			.findAny()
			.orElse(null);
	}

	public ObjectNode getScheduleByName(String homeId, String name) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getScheduleByName(getSchedules(homeId), name);
	}

	public static ObjectNode getScheduleByName(ObjectNode homeData, String name) {
		return getScheduleByName(getSchedules(homeData), name);
	}

	public static ObjectNode getScheduleByName(List<ObjectNode> schedules, String name) {
		return schedules.stream()
			.filter(s -> Objects.equals(s.at("/name").textValue(), name))
			.findAny()
			.orElse(null);
	}

	public JsonNode setSchedule(ObjectNode schedule) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return setSchedule(schedule, null);
	}

	public JsonNode setSchedule(ObjectNode schedule, String homeId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		if (!schedule.has(SCHEDULE_ID)) {
			schedule.put(SCHEDULE_ID, schedule.at("/id").textValue());
		}
		if (!schedule.has(HOME_ID)) {
			assert homeId != null;
			schedule.put(HOME_ID, homeId);
		}
		schedule.remove("id");
		schedule.remove("default");
		schedule.remove("type");
		IterableUtils.toList(schedule.at("/zones")).stream()
			.map(ObjectNode.class::cast)
			.forEach(it -> it.remove("rooms_temp"));
		return OBJECT_MAPPER.readTree(post("synchomeschedule", null, OBJECT_MAPPER.writeValueAsString(schedule), "application/json"));
	}

}
