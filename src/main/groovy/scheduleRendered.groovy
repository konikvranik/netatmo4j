@Grab('info.picocli:picocli:4.7.3')
@picocli.CommandLine.Command(name = "myCommand", description = "does something special")
@picocli.groovy.PicocliScript2

import com.fasterxml.jackson.databind.json.JsonMapper
import groovy.transform.Field
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import net.suteren.netatmo.domain.therm.Schedule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.simple.SimpleLogger
import picocli.CommandLine

import java.nio.charset.Charset

@Field final Logger log = LoggerFactory.getLogger('scheduleRenderer')

@CommandLine.Parameters(arity = "1", paramLabel = "SECHEDULE", description = "Schedule JSON file")
@Field File scheduleFile

@CommandLine.Option(names = ["-d", "--debug"], description = "Enable verbose logging")
@Field boolean debug = false
if (debug) {
	System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
}

@Field final static JSON_MAPPER = JsonMapper.builder().build()

Schedule schedule = JSON_MAPPER.readValue(scheduleFile, Schedule)


//======================================================================//
String source = "@startuml\n";
source += "Bob -> Alice : hello\n";
source += "@enduml\n";

SourceStringReader reader = new SourceStringReader(source);
final ByteArrayOutputStream os = new ByteArrayOutputStream();
// Write the first image to "os"
String desc = reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
log.info(desc)
os.close();

// The XML is stored into svg
final String svg = new String(os.toByteArray(), Charset.forName("UTF-8"));

log.info(svg)