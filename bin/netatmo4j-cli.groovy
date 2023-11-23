#!/usr/bin/env groovy
import net.suteren.netatmo.cli.NetatmoCli
import picocli.CommandLine

@GrabConfig(systemClassLoader = true)
@Grapes([
		@Grab('net.suteren.netatmo:netatmo4j:0.4.0'),
])

CommandLine cli = new CommandLine(new NetatmoCli())
System.exit(cli.execute(args))
