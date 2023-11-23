package net.suteren.netatmo.cli.command;

import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import net.suteren.netatmo.Version;
import net.suteren.netatmo.auth.AuthClient;
import net.suteren.netatmo.cli.CliCfg;
import net.suteren.netatmo.therm.HomeClient;
import picocli.CommandLine;

@CommandLine.Command(mixinStandardHelpOptions = true, versionProvider = Version.VersionProvider.class)
@Getter public class AbstractCommand implements Callable<Integer> {

	public static final String COMMAND_NOT_SPECIFIED_MESSAGE = "command not specified";
	@CommandLine.ParentCommand AbstractCommand parentCommand;

	protected HomeClient getHomeClient() {
		return parentCommand.getHomeClient();
	}

	protected AuthClient getAuthClient() {
		return parentCommand.getAuthClient();
	}

	protected CliCfg getCfg() {
		return parentCommand.getCfg();
	}

	protected String getHomeId() {
		return parentCommand.getHomeId();
	}

	protected ObjectMapper getJsonMapper() {
		return parentCommand.getJsonMapper();
	}

	protected ObjectMapper getYamlMapper() {
		return parentCommand.getYamlMapper();
	}

	@Override public Integer call() throws Exception {
		throw new CommandLine.ParameterException(new CommandLine(this), COMMAND_NOT_SPECIFIED_MESSAGE);
	}

	public void println(String text) {
		new CommandLine(this).getOut().println(text);
	}
}
