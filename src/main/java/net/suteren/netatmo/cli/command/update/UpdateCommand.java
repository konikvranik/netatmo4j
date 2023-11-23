package net.suteren.netatmo.cli.command.update;

import net.suteren.netatmo.cli.command.AbstractCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "update", subcommands = { UpdateScheduleCommand.class })
public class UpdateCommand extends AbstractCommand {
	@Override public Integer call() {
		throw new CommandLine.ParameterException(new CommandLine(this), "command not specified");
	}
}