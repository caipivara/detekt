package io.gitlab.arturbosch.detekt.cli.baseline

import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.SAXParserFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamWriter
import org.xml.sax.SAXParseException

/**
 * @author Artur Bosch
 */
class BaselineFormat {

    fun read(path: Path): Baseline {
        try {
            Files.newInputStream(path).use {
                val reader = SAXParserFactory.newInstance().newSAXParser()
                val handler = BaselineHandler()
                reader.parse(it, handler)
                return handler.createBaseline()
            }
        } catch (error: SAXParseException) {
            val (line, column) = error.lineNumber to error.columnNumber
            throw InvalidBaselineState("Error on position $line:$column while reading the baseline xml file!", error)
        }
    }

    fun write(baseline: Baseline, path: Path) {
        try {
            Files.newBufferedWriter(path).use {
                it.streamXml().prettyPrinter().save(baseline)
            }
        } catch (error: XMLStreamException) {
            val (line, column) = error.positions
            throw InvalidBaselineState("Error on position $line:$column while writing the baseline xml file!", error)
        }
    }

    private val XMLStreamException.positions
        get() = location.lineNumber to location.columnNumber

    private fun XMLStreamWriter.save(baseline: Baseline) {
        document {
            tag(SMELL_BASELINE) {
                tag(BLACKLIST) {
                    val (ids, timestamp) = baseline.blacklist
                    attribute(TIMESTAMP, timestamp)
                    ids.forEach { tag(ID, it) }
                }
                tag(WHITELIST) {
                    val (ids, timestamp) = baseline.whitelist
                    attribute(TIMESTAMP, timestamp)
                    ids.forEach { tag(ID, it) }
                }
            }
        }
    }
}
