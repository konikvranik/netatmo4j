package net.suteren.netatmo.cli.command.list;

import java.util.Collection;

import picocli.CommandLine;

@CommandLine.Command(name = "list", subcommands = { ListHomesCommand.class, ListRoomsCommand.class, ListSchedulesCommand.class })
public class ListCommand extends AbstractListCommand<Void> {

	@Override public Integer call() {
		throw new CommandLine.ParameterException(new CommandLine(this), COMMAND_NOT_SPECIFIED_MESSAGE);
	}

	@Override protected Collection<Void> getObjects() {
		throw new UnsupportedOperationException();
	}

	public String format(Void object) {
		throw new UnsupportedOperationException();
	}
}
