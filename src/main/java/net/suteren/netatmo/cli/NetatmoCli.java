package net.suteren.netatmo.cli;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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
import net.suteren.netatmo.domain.therm.Home;
import net.suteren.netatmo.domain.therm.HomesData;
import net.suteren.netatmo.domain.therm.Room;
import net.suteren.netatmo.therm.HomeClient;
import net.suteren.netatmo.therm.ScheduleClient;
import picocli.CommandLine;

import static net.suteren.netatmo.auth.AuthClient.Scope.READ_THERMOSTAT;
import static net.suteren.netatmo.auth.AuthClient.Scope.WRITE_THERMOSTAT;

@Slf4j
@CommandLine.Command(name = "checksum", mixinStandardHelpOptions = true, version = "checksum 4.0",
	description = "Prints the checksum (SHA-256 by default) of a file to STDOUT.")
public final class NetatmoCli implements Callable<Integer> {

	private static final ObjectMapper YAML_OBJECT_MAPPER = YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build();
	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();
	public static final String HOME_S_IS_NOT_PRESENT_MESSAGE = "Home %s is not present.";

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
			AuthClient authClient = new AuthClient(clientId, clientSecret, List.of(READ_THERMOSTAT, WRITE_THERMOSTAT), "Netatmo tool", authconfig);
			CliCfg cfg = YAML_OBJECT_MAPPER.readValue(config, CliCfg.class);
			if (homeId == null) {
				homeId = cfg.homeId();
			}

			HomesData homesData = new HomeClient(authClient).getHomesData();
			Optional<Home> currentHome = homesData.getHomeById(homeId);
			switch (command) {
			case "list":
				switch (arguments[0]) {
				case "homes":
					List<Home> homes = homesData.homes();
					switch (format) {
					case "text":
						System.out.println(homes.stream()
							.map(h -> "%25s : %s".formatted(h.id(), h.name()))
							.collect(Collectors.joining("\n")));
						break;
					case "json":
						System.out.println(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(homes));
						break;
					case "yaml":
						System.out.println(YAML_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(homes));
						break;
					default:
						throw new IllegalStateException("Unexpected value: " + format);
					}
					break;
				case "rooms":
					List<Room> rooms = currentHome
						.orElseThrow(() -> new NoSuchElementException(HOME_S_IS_NOT_PRESENT_MESSAGE.formatted(homeId)))
						.rooms();
					switch (format) {
					case "text":
						System.out.println(rooms.stream()
							.map(h -> "%15s : %s".formatted(h.id(), h.name()))
							.collect(Collectors.joining("\n")));
						break;
					case "json":
						System.out.println(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(rooms));
						break;
					case "yaml":
						System.out.println(YAML_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(rooms));
						break;
					default:
						throw new IllegalStateException("Unexpected value: " + format);
					}
					break;
				case "schedules":
					switch (format) {
					case "text":
						System.out.println(currentHome
							.orElseThrow(() -> new NoSuchElementException(HOME_S_IS_NOT_PRESENT_MESSAGE.formatted(homeId)))
							.schedules().stream()
							.map(h -> "%25s : %s".formatted(h.getId(), h.name()))
							.collect(Collectors.joining("\n")));
						break;
					case "json":
						System.out.println(
							OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(currentHome
								.orElseThrow(() -> new NoSuchElementException(HOME_S_IS_NOT_PRESENT_MESSAGE.formatted(homeId)))
								.schedules().stream()
								.toList()));
						break;
					case "yaml":
						System.out.println(
							YAML_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(currentHome
								.orElseThrow(() -> new NoSuchElementException(HOME_S_IS_NOT_PRESENT_MESSAGE.formatted(homeId)))
								.schedules().stream()
								.toList()));
						break;
					default:
						throw new IllegalStateException("Unexpected value: " + format);
					}
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + arguments[0]);
				}
				break;
			case "get":
				switch (arguments[0]) {
				case "schedules":
					switch (format) {
					case "text":
						System.out.println(currentHome
							.orElseThrow(() -> new NoSuchElementException(HOME_S_IS_NOT_PRESENT_MESSAGE.formatted(homeId)))
							.schedules().stream()
							.map(h -> "%25s : %s".formatted(h.getId(), h.name()))
							.collect(Collectors.joining("\n")));
						break;
					case "json":
						System.out.println(
							OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(currentHome
								.orElseThrow(() -> new NoSuchElementException(HOME_S_IS_NOT_PRESENT_MESSAGE.formatted(homeId)))
								.schedules().stream()
								.toList()));
						break;
					case "yaml":
						System.out.println(
							YAML_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(currentHome
								.orElseThrow(() -> new NoSuchElementException(HOME_S_IS_NOT_PRESENT_MESSAGE.formatted(homeId)))
								.schedules().stream()
								.toList()));
						break;
					default:
						throw new IllegalStateException("Unexpected value: " + format);
					}
					break;
				case "homesdata":
					switch (format) {
					case "text":
						System.out.println(homesData.homes().stream()
							.map(h -> "%25s : %s".formatted(h.id(), h.name()))
							.collect(Collectors.joining("\n")));
						break;
					case "json":
						System.out.println(
							OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(homesData));
						break;
					case "yaml":
						System.out.println(YAML_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(homesData));
						break;
					default:
						throw new IllegalStateException("Unexpected value: " + format);
					}
					break;

				default:
					throw new IllegalStateException("Unexpected value: " + arguments[0]);
				}
				break;
			case "update":
				ZonePresets zonePresets = new ZonePresets(cfg);
				ScheduleClient scheduleClient = new ScheduleClient(authClient);
				currentHome.ifPresentOrElse(h -> h.schedules().forEach(schedule -> {
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
							throw new UnexpectedException(e);
						} catch (ConnectionException e) {
							log.error("%d: %s".formatted(e.getErrorInfo().code(), e.getErrorInfo().message()));
							throw new UnexpectedException(e);
						}
					}),
					() -> {throw new NoSuchElementException(HOME_S_IS_NOT_PRESENT_MESSAGE.formatted(homeId));}
				);
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + command);
			}
		} catch (ConnectionException e) {
			log.error("%d: %s".formatted(e.getErrorInfo().code(), e.getErrorInfo().message()));
			throw e;
		}
		return 0;
	}

	public static void main(String... args) {
		System.exit(new CommandLine(new NetatmoCli()).execute(args));
	}
}
