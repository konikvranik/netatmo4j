#!/usr/bin/env groovy
@GrabResolver(name = 'netatmo4j', root = 'https://s01.oss.sonatype.org/content/repositories/snapshots/')
@GrabConfig(systemClassLoader = true)
@Grapes([
		@Grab('info.picocli:picocli:4.7.3'),
		@Grab('info.picocli:picocli-groovy:4.7.3'),
		@Grab('org.slf4j:slf4j-api:2.0.7'),
		@Grab('org.slf4j:slf4j-simple:2.0.7'),
		@Grab('net.sourceforge.plantuml:plantuml:1.2023.10'),
		@Grab('net.suteren.netatmo:netatmo4j:1.0-SNAPSHOT')
])
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

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal
import java.util.concurrent.atomic.AtomicInteger

@Field final static JSON_MAPPER = JsonMapper.builder().build()
@Field final static Map<Integer, String> ZONE_COLORS = [0: "GreenYellow", 1: "MediumSlateBlue", 2: "yellow", 3: "orange", 4: "cyan"]
@Field Map<Integer, String> zoneNames

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

@Field static final DateTimeFormatter WEEKDAY_FORMATTER = DateTimeFormatter.ofPattern('ccc')
@Field static final Temporal WEEKDAY_TEMPORAL = LocalDate.now()

Schedule schedule = JSON_MAPPER.readValue(scheduleFile, Schedule)
zoneNames = schedule.zones().collectEntries { [(it.id()): it.name()] }
List<TimetableEntry> timetable = schedule.timetable()
def days = timetable.groupBy { it.mOffset() / MINUTES_PER_DAY as int }

String source = """
@startuml

title Weekly schedule: ${schedule.name()}

concise "${formatDow(DayOfWeek.MONDAY)}" as d0
concise "${formatDow(DayOfWeek.TUESDAY)}" as d1
concise "${formatDow(DayOfWeek.WEDNESDAY)}" as d2
concise "${formatDow(DayOfWeek.THURSDAY)}" as d3
concise "${formatDow(DayOfWeek.FRIDAY)}" as d4
concise "${formatDow(DayOfWeek.SATURDAY)}" as d5
concise "${formatDow(DayOfWeek.SUNDAY)}" as d6

${renderDays(days)}

@enduml
"""

private static String formatDow(DayOfWeek dow) {
	WEEKDAY_FORMATTER.format(WEEKDAY_TEMPORAL.with(dow))
}

if (!renderImage) {
	println source
} else {
	final ByteArrayOutputStream os = new ByteArrayOutputStream()
	log.info(new SourceStringReader(source).outputImage(os, new FileFormatOption(FileFormat.SVG)).toString())
	os.close()
	println os.toString('UTF-8')
}

private String renderDays(Map<Integer, List<TimetableEntry>> days) {
	AtomicInteger last = new AtomicInteger(days.get(0).first().zoneId())
	days.collect { d, t -> "@d${d}\n${renderTimes(t, last)}" }.join('\n\n')
}

private String renderTimes(List<TimetableEntry> timetableEntries, AtomicInteger last) {
	"0:00:00 is \"${zoneNames[last.get()]}\" #${getColor(last.getAndSet(timetableEntries.last().zoneId()))}\n" + timetableEntries.collect { "${offsetToTime(it)} is \"${zoneNames[it.zoneId()]}\" #${getColor(it.zoneId())}" }.join('\n') + "\n24:00:0 is {hidden}"
}

private static String getColor(int set) {
	ZONE_COLORS[set] ?: "Lavender"
}

private static String offsetToTime(TimetableEntry it) {
	def minutes = it.mOffset() % MINUTES_PER_DAY
	"${minutes / 60 as int}:${minutes % 60}:00"
}
