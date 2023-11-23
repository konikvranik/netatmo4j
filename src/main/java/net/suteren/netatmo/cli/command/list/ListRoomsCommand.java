package net.suteren.netatmo.cli.command.list;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import net.suteren.netatmo.client.ConnectionException;
import net.suteren.netatmo.domain.therm.Home;
import net.suteren.netatmo.domain.therm.Room;
import picocli.CommandLine;

@CommandLine.Command(name = "rooms")
class ListRoomsCommand extends AbstractListCommand<Room> {
	@Override protected Collection<Room> getObjects() throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getHomeClient().getHomesData().getHomeById(getHomeId())
			.map(Home::rooms)
			.stream()
			.flatMap(Collection::stream)
			.toList();
	}

	@Override public String format(Room home) {
		return String.format("%25s : %s", home.id(), home.name());
	}
}

