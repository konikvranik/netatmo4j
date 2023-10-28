@Grab('info.picocli:picocli:4.7.3')
@picocli.CommandLine.Command(name = "myCommand", description = "does something special")
@picocli.groovy.PicocliScript2

import com.fasterxml.jackson.databind.json.JsonMapper
import groovy.transform.Field
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import net.suteren.netatmo.domain.therm.Schedule
import net.suteren.netatmo.domain.therm.TimetableEntry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.simple.SimpleLogger
import picocli.CommandLine

import java.nio.charset.Charset

@Field final Logger log = LoggerFactory.getLogger('scheduleRenderer')
@Field public static final int MINUTES_PER_DAY = 1440

@CommandLine.Parameters(arity = "1", paramLabel = "SECHEDULE", description = "Schedule JSON file")
@Field File scheduleFile

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

${initDays(days)}

${renderDays(days)}
@enduml
"""

println source
//======================================================================//

SourceStringReader reader = new SourceStringReader(source);
final ByteArrayOutputStream os = new ByteArrayOutputStream();
// Write the first image to "os"
String desc = reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
log.info(desc)
os.close();

// The XML is stored into svg
final String svg = new String(os.toByteArray(), Charset.forName("UTF-8"));

log.info(svg)

String renderDays(Map<BigDecimal, List<TimetableEntry>> days) {
	days.collect { d, t ->
		"""
@d${d}
${renderTimes(t)}
"""
	}
			.join('\n\n')
}

String renderTimes(List<TimetableEntry> timetableEntries) {
	timetableEntries.collect { "${offsetToTime(it)} is \"${zoneNames[it.zoneId()]}\" #${zoneColors[it.zoneId()]}" }
			.join('\n')
}

private static String offsetToTime(TimetableEntry it) {
	def minutes = it.mOffset() % MINUTES_PER_DAY
	"${minutes / 60 as int}:${minutes % 60}:00"
}

String initDays(Map<Integer, List<TimetableEntry>> days) {
	days
			.findAll { it.key < 6 }
			.collect { d, t -> "d${d + 1} is \"${zoneNames[(t.last().zoneId())]}\" #${zoneColors[t.last().zoneId()]}" }
			.join('\n')

}