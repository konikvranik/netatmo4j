package net.suteren.netatmo.cli;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.simple.SimpleLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.suteren.netatmo.auth.AuthClient;
import net.suteren.netatmo.cli.command.AbstractCommand;
import net.suteren.netatmo.cli.command.list.ListCommand;
import net.suteren.netatmo.cli.command.set.SetCommand;
import net.suteren.netatmo.cli.command.update.UpdateCommand;
import net.suteren.netatmo.therm.HomeClient;
import picocli.CommandLine;

import static net.suteren.netatmo.auth.AuthClient.Scope.READ_THERMOSTAT;
import static net.suteren.netatmo.auth.AuthClient.Scope.WRITE_THERMOSTAT;

@Slf4j
@CommandLine.Command(name = "netatmo4j-cli", description = "Commandline client to the Netatmo API", subcommands = { UpdateCommand.class,
	ListCommand.class, SetCommand.class })
public final class NetatmoCli extends AbstractCommand {
	@CommandLine.Option(names = { "-d", "--debug" }, description = "Enable verbose logging")
	private boolean debug = false;

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

	@Getter final ObjectMapper jsonMapper = JsonMapper.builder().build();
	@Getter final ObjectMapper yamlMapper = YAMLMapper.builder().build();

	private AuthClient authClient;
	private HomeClient homeClient;
	private CliCfg cfg;

	public NetatmoCli() {
		if (debug) {
			System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");
		}
	}

	@SneakyThrows
	@Override public HomeClient getHomeClient() {
		if (homeClient == null) {
			homeClient = new HomeClient(getAuthClient());
		}
		return homeClient;
	}

	@SneakyThrows
	@Override public AuthClient getAuthClient() {
		if (authClient == null) {
			authClient = new AuthClient(clientId, clientSecret, List.of(READ_THERMOSTAT, WRITE_THERMOSTAT), "Netatmo tool", authconfig);
		}
		return authClient;
	}

	@SneakyThrows
	@Override public String getHomeId() {
		if (homeId == null) {
			homeId = getCfg().homeId();
		}
		return homeId;
	}

	@Override public CliCfg getCfg() {
		if (cfg == null) {
			try {
				cfg = yamlMapper.readValue(config, CliCfg.class);
			} catch (IOException e) {
				log.error("There is a problem reading the config file: ".concat(e.getMessage()));
				System.exit(1);
				throw new RuntimeException(e);
			}
		}
		return cfg;
	}

	public static void main(String... args) {
		System.exit(new CommandLine(new NetatmoCli()).execute(args));
	}
}
