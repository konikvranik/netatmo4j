package net.suteren.netatmo.cli;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.simple.SimpleLogger;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import lombok.extern.slf4j.Slf4j;
import net.suteren.netatmo.ZonePresets;
import net.suteren.netatmo.auth.AuthClient;
import net.suteren.netatmo.client.ConnectionException;
import net.suteren.netatmo.client.NetatmoResponse;
import net.suteren.netatmo.domain.therm.Schedule;
import net.suteren.netatmo.therm.HomeClient;
import net.suteren.netatmo.therm.ScheduleClient;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "checksum", mixinStandardHelpOptions = true, version = "checksum 4.0",
	description = "Prints the checksum (SHA-256 by default) of a file to STDOUT.")
public class NetatmoCli implements Callable<Integer> {

	private static final ObjectMapper YAML_OBJECT_MAPPER = YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build();
	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

	@CommandLine.Parameters(arity = "1", index = "0", paramLabel = "COMMAND", description = "Command to execute")
	private String command;

	@CommandLine.Parameters(arity = "0..", index = "1..", paramLabel = "ARGUMENTS", description = "Additional arguments to the command")
	private String[] arguments;

	@CommandLine.Option(names = { "-d", "--debug" }, description = "Enable verbose logging")
	private boolean debug = false;

	@CommandLine.Option(names = { "-f", "--format" }, description = "Output format")
	private String format = "text";

	@CommandLine.Option(names = { "-c", "--config" }, description = "config file")
	private File config = new File(new File(System.getProperties().getProperty("user.home")), ".netatmorc.yaml");

	@CommandLine.Option(names = { "-a", "--authconfig" }, description = "auth config file")
	private File authconfig = new File(new File(System.getProperties().getProperty("user.home")), ".netatmoauth.json");

	@CommandLine.Option(names = { "-u", "--clientid" }, description = "client ID")
	private String clientId;

	@CommandLine.Option(names = { "-p", "--clientsecret" }, description = "client secret")
	private String clientSecret;

	@CommandLine.Option(names = { "-i", "--home_id" }, description = "Id of the Home")
	private String homeId;

	@CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "Show this help message and exit.")
	private boolean helpRequested;

	@Override
	public Integer call() throws Exception { // your business logic goes here...
		if (debug) {
			System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");
		}

		try {
			AuthClient authClient = new AuthClient(clientId, clientSecret, List.of("read_thermostat", "write_thermostat"), "Netatmo tool", authconfig);
			CliCfg cfg = YAML_OBJECT_MAPPER.readValue(config, CliCfg.class);
			if (homeId == null) {
				homeId = cfg.homeId();
			}

			HomeClient homeClient = new HomeClient(authClient);
			switch (command) {
			case "list":
				switch (arguments[0]) {
				case "homes":
					switch (format) {
					case "text":
						System.out.println(homeClient.listHomes().stream()
							.map(h -> "%25s : %s".formatted(h.at("/id").textValue(), h.at("/name").textValue()))
							.collect(Collectors.joining("\n")));
						break;
					case "json":
						System.out.println(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(homeClient.listHomes()));
						break;
					case "yaml":
						System.out.println(YAML_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(homeClient.listHomes()));
						break;
					}
					break;
				case "rooms":
					switch (format) {
					case "text":
						System.out.println(homeClient.getRooms(homeId).stream()
							.map(h -> "%15s : %s".formatted(h.at("/id").textValue(), h.at("/name").textValue()))
							.collect(Collectors.joining("\n")));
						break;
					case "json":
						System.out.println(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(homeClient.getRooms(homeId).stream()
							.peek(r -> r.remove("'module_ids'"))
							.toList()));
						break;
					case "yaml":
						System.out.println(YAML_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(homeClient.getRooms(homeId).stream()
							.peek(r -> r.remove("'module_ids'"))
							.toList()));
						break;
					}
					break;
				case "schedules":
					switch (format) {
					case "text":
						System.out.println(new ScheduleClient(authClient).getSchedules(homeId).stream()
							.map(h -> "%25s : %s".formatted(h.getId(), h.name()))
							.collect(Collectors.joining("\n")));
						break;
					case "json":
						System.out.println(
							OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(new ScheduleClient(authClient).getSchedules(homeId).stream()
								.toList()));
						break;
					case "yaml":
						System.out.println(
							YAML_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(new ScheduleClient(authClient).getSchedules(homeId).stream()
								.toList()));
						break;
					}
					break;
				}
				break;
			case "get":
				switch (arguments[0]) {
				case "schedules":
					switch (format) {
					case "text":
						System.out.println(new ScheduleClient(authClient).getSchedules(homeId).stream()
							.map(h -> "%25s : %s".formatted(h.getId(), h.name()))
							.collect(Collectors.joining("\n")));
						break;
					case "json":
						System.out.println(
							OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(new ScheduleClient(authClient).getSchedules(homeId).stream()
								.toList()));
						break;
					case "yaml":
						System.out.println(
							YAML_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(new ScheduleClient(authClient).getSchedules(homeId).stream()
								.toList()));
						break;
					}
					break;
				case "homesdata":
					switch (format) {
					case "text":
						System.out.println(homeClient.getHomesData()
							.homes().stream()
							.map(h -> "%25s : %s".formatted(h.id(), h.name()))
							.collect(Collectors.joining("\n")));
						break;
					case "json":
						System.out.println(
							OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(homeClient.getHomesData()));
						break;
					case "yaml":
						System.out.println(YAML_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(homeClient.getHomesData()));
						break;
					}
					break;

				}
				break;
			case "update":
				ZonePresets zonePresets = new ZonePresets(cfg);
				ScheduleClient scheduleClient = new ScheduleClient(authClient);
				List<Schedule> schedules = scheduleClient.getSchedules(homeId);
				schedules.forEach(schedule -> {
					String scheduleId = schedule.getId();
					schedule = schedule.toBuilder()
						.zones(schedule.zones().stream()
							.map(z -> z.toBuilder().rooms(z.rooms().stream()
								.map(r -> r.toBuilder().thermSetpointTemperature(zonePresets.getTemp(scheduleId, z.id(), r.id())).build())
								.toList()).build())
							.toList()).build();
					try {
						NetatmoResponse response = scheduleClient.updateSchedule(schedule, homeId);
						log.info("{}: {} in {} seconds", response.getServerTime().toString(), response.status(), response.timeExec());
					} catch (IOException | InterruptedException | URISyntaxException e) {
						log.error(e.getMessage(), e);
						throw new RuntimeException(e);
					} catch (ConnectionException e) {
						try {
							log.error(IOUtils.toString(e.getConnection().getErrorStream()));
						} catch (IOException ex) {
							log.error(e.getMessage(), e);
						}
						throw new RuntimeException(e);
					}
				});
				break;
			}
		} catch (ConnectionException e) {
			System.out.println("status: $e.connection.responseCode");
			System.out.println("response: $e.connection.errorStream.text");
			throw e;
		}
		return 0;
	}

	public static void main(String... args) {
		System.exit(new CommandLine(new NetatmoCli()).execute(args));
	}
}
