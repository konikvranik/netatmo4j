package net.suteren.netatmo.cli.command.set;

import net.suteren.netatmo.cli.command.AbstractCommand;
import net.suteren.netatmo.therm.ScheduleClient;
import picocli.CommandLine;

@CommandLine.Command(name = "schedule")
public class SetScheduleCommand extends AbstractCommand {

	@CommandLine.Parameters(arity = "1", index = "0", paramLabel = "SCHEDULE", description = "Schedule ID")
	String scheduleId;

	@Override public Integer call() throws Exception {
		ScheduleClient scheduleClient = new ScheduleClient(getAuthClient());
		scheduleClient.setSchedule(getHomeId(), scheduleId);
		return 0;
	}
}