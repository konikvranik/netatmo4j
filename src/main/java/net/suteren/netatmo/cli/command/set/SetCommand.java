package net.suteren.netatmo.cli.command.set;

import net.suteren.netatmo.cli.command.AbstractCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "set", subcommands = { SetScheduleCommand.class })
public class SetCommand extends AbstractCommand {

	@Override public Integer call() {
		throw new CommandLine.ParameterException(new CommandLine(this), "command not specified");
	}
}