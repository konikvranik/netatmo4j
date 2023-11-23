package net.suteren.netatmo.cli.command.list;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import net.suteren.netatmo.cli.command.AbstractCommand;
import net.suteren.netatmo.client.ConnectionException;
import picocli.CommandLine;

public abstract class AbstractListCommand<T> extends AbstractCommand {
	@CommandLine.Option(names = { "-f", "--format" }, description = "Output format")
	String format;

	@CommandLine.Option(names = { "-p", "--pretty-print" }, description = "Pretty print for formats which supports it like JSON.")
	Boolean prettyPrint;

	String getFormat() {
		if (StringUtils.isNotEmpty(format))
			return format;
		return getParentCommand() instanceof AbstractListCommand<?> parentListCommand
			? parentListCommand.getFormat()
			: "text";
	}

	boolean getPrettyPrint() {
		if (prettyPrint != null)
			return prettyPrint;
		return getParentCommand() instanceof AbstractListCommand<?> parentListCommand && parentListCommand.getPrettyPrint();
	}

	protected abstract Collection<T> getObjects() throws IOException, URISyntaxException, InterruptedException, ConnectionException;

	public abstract String format(T object);

	@Override public Integer call() throws Exception {
		Object objects = getObjects();
		return switch (getFormat()) {
			case "text" -> {
				println(getObjects().stream()
					.map(this::format)
					.collect(Collectors.joining("\n"))
				);
				yield 0;
			}
			case "json" -> {
				if (getPrettyPrint()) {
					println(getJsonMapper().writerWithDefaultPrettyPrinter().writeValueAsString(objects));
				} else {
					println(getJsonMapper().writeValueAsString(objects));
				}
				yield 0;
			}
			case "yaml" -> {
				if (getPrettyPrint()) {
					println(getYamlMapper().writerWithDefaultPrettyPrinter().writeValueAsString(objects));
				} else {
					println(getYamlMapper().writeValueAsString(objects));
				}
				yield 0;
			}
			default -> 2;
		};
	}
}
