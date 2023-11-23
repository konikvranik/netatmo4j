package net.suteren.netatmo.cli.command.list;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import net.suteren.netatmo.client.ConnectionException;
import net.suteren.netatmo.domain.therm.Home;
import picocli.CommandLine;

@CommandLine.Command(name = "homes")
public class ListHomesCommand extends AbstractListCommand<Home> {
	@Override protected Collection<Home> getObjects() throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getHomeClient().getHomesData().homes();
	}

	@Override public String format(Home home) {
		return String.format("%25s : %s", home.id(), home.name());
	}
}
