@Grab('info.picocli:picocli:4.7.3')
@picocli.CommandLine.Command(name = "myCommand", description = "does something special")
@picocli.groovy.PicocliScript2

import com.fasterxml.jackson.databind.json.JsonMapper
import groovy.transform.Field
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.core.DiagramDescription
import net.suteren.netatmo.domain.therm.Schedule
import net.suteren.netatmo.domain.therm.TimetableEntry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.simple.SimpleLogger
import picocli.CommandLine

import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicInteger

@Field final Logger log = LoggerFactory.getLogger('scheduleRenderer')
@Field public static final int MINUTES_PER_DAY = 1440

@CommandLine.Parameters(arity = "1", paramLabel = "SECHEDULE", description = "Schedule JSON file")
@Field File scheduleFile

@CommandLine.Option(names = ["-i", "--renderImage"], description = "Render image or get plantuml source")
@Field boolean renderImage = false

@CommandLine.Option(names = ["-d", "--debug"], description = "Enable verbose logging")
@Field boolean debug = false
if (debug) {
	System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
}

@Field final static JSON_MAPPER = JsonMapper.builder().build()
@Field Map<Integer, String> zoneNames
@Field Map<Integer, String> zoneColors = [0: "GreenYellow", 1: "MediumSlateBlue", 2: "yellow", 3: "orange", 4: "cyan"]

Schedule schedule = JSON_MAPPER.readValue(scheduleFile, Schedule)
zoneNames = schedule.zones().collectEntries { [(it.id()): it.name()] }
List<TimetableEntry> timetable = schedule.timetable()
def days = timetable.groupBy { it.mOffset() / MINUTES_PER_DAY as int }

String source = """
@startuml

title Weekly schedule: ${schedule.name()}

concise "Mo" as d0
concise "Tu" as d1
concise "We" as d2
concise "Th" as d3
concise "Fr" as d4
concise "Sa" as d5
concise "Fr" as d6

${renderDays(days)}

@enduml
"""

if (!renderImage) {
	println source
} else {
//======================================================================//

	SourceStringReader reader = new SourceStringReader(source);
	final ByteArrayOutputStream os = new ByteArrayOutputStream();
// Write the first image to "os"
	DiagramDescription desc = reader.outputImage(os, new FileFormatOption(FileFormat.SVG));
	log.info(desc.toString())
	os.close();

// The XML is stored into svg
	final String svg = new String(os.toByteArray(), Charset.forName("UTF-8"));

	println svg
}

String renderDays(Map<BigDecimal, List<TimetableEntry>> days) {
	AtomicInteger last = new AtomicInteger(days.get(0).first().zoneId())
	days.collect { d, t -> "@d${d}\n${renderTimes(t, last)}" }.join('\n\n')
}

String renderTimes(List<TimetableEntry> timetableEntries, AtomicInteger last) {
	"0:00:00 is \"${zoneNames[last.get()]}\" #${zoneColors[last.getAndSet(timetableEntries.last().zoneId())]}\n" + timetableEntries.collect { "${offsetToTime(it)} is \"${zoneNames[it.zoneId()]}\" #${zoneColors[it.zoneId()]}" }.join('\n') + "\n24:00:0 is {hidden}"
}

private static String offsetToTime(TimetableEntry it) {
	def minutes = it.mOffset() % MINUTES_PER_DAY
	"${minutes / 60 as int}:${minutes % 60}:00"
}
