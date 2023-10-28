package net.suteren.netatmo.therm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections4.IterableUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.SneakyThrows;
import net.suteren.netatmo.auth.AuthClient;
import net.suteren.netatmo.client.AbstractApiClient;
import net.suteren.netatmo.client.ConnectionException;
import net.suteren.netatmo.domain.therm.Schedule;

public class ScheduleClient extends AbstractApiClient<InputStream> {

	public static final java.lang.String SCHEDULE_ID = "schedule_id";
	public static final java.lang.String HOME_ID = "home_id";
	private final HomeClient homeClient;

	public ScheduleClient(AuthClient auth) {
		super(auth);
		homeClient = new HomeClient(auth);
	}

	public List<Schedule> getSchedules(String homeId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getSchedules(homeClient.getHome(homeId));
	}

	public static List<Schedule> getSchedules(ObjectNode homeData) {
		return IterableUtils.toList(homeData.at("/schedules")).stream()
			.map(ScheduleClient::readSchedule)
			.toList();
	}

	public Schedule getSchedule(String homeId, String scheduleId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getSchedule(getSchedules(homeId), scheduleId);
	}

	public static Schedule getSchedule(ObjectNode homeData, String scheduleId) {
		return getSchedule(getSchedules(homeData), scheduleId);
	}

	public static Schedule getSchedule(List<Schedule> schedules, String scheduleId) {
		return schedules.stream()
			.filter(s -> Objects.equals(s.getId(), scheduleId))
			.findAny()
			.orElse(null);
	}

	public Schedule getScheduleByName(String homeId, String name) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getScheduleByName(getSchedules(homeId), name);
	}

	public static Schedule getScheduleByName(ObjectNode homeData, String name) {
		return getScheduleByName(getSchedules(homeData), name);
	}

	public static Schedule getScheduleByName(List<Schedule> schedules, String name) {
		return schedules.stream()
			.filter(s -> Objects.equals(s.name(), name))
			.findAny()
			.orElse(null);
	}

	public JsonNode setSchedule(Schedule schedule) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return setSchedule(schedule, null);
	}

	public JsonNode setSchedule(Schedule schedule, String homeId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		schedule = schedule.toBuilder()
			.scheduleId(schedule.getId())
			.id(null)
			.isDefaultZone(null)
			.type(null)
			.homeId(Optional.ofNullable(schedule.homeId())
				.filter(string -> !string.isBlank())
				.orElse(homeId))
			.zones(schedule.zones().stream().map(z -> z.toBuilder().roomstemp(null).build()).toList())
			.build();
		return OBJECT_MAPPER.readTree(post("synchomeschedule", null, OBJECT_MAPPER.writeValueAsString(schedule), "application/json"));
	}

	@SneakyThrows private static Schedule readSchedule(JsonNode o) {
		return OBJECT_MAPPER.treeToValue(o, Schedule.class);
	}
}
