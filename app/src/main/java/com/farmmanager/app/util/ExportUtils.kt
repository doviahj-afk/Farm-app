package com.farmmanager.app.util

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Minimal spreadsheet export helpers — no external libraries required (keeps the app light,
 * important since this project is built/compiled from a phone). CSV is trivial text.
 * XLSX is written by hand as a small valid Office Open XML package (a zip of a few XML files),
 * using inline strings so no sharedStrings.xml bookkeeping is needed.
 */
object ExportUtils {

    fun writeCsv(output: OutputStream, headers: List<String>, rows: List<List<String>>) {
        output.bufferedWriter().use { writer ->
            writer.write(headers.joinToString(",") { csvEscape(it) })
            writer.newLine()
            rows.forEach { row ->
                writer.write(row.joinToString(",") { csvEscape(it) })
                writer.newLine()
            }
        }
    }

    private fun csvEscape(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else value
    }

    fun writeXlsx(output: OutputStream, sheetName: String, headers: List<String>, rows: List<List<String>>) {
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("[Content_Types].xml"))
            zip.write(CONTENT_TYPES.toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("_rels/.rels"))
            zip.write(RELS.toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/workbook.xml"))
            zip.write(workbookXml(sheetName).toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            zip.write(WORKBOOK_RELS.toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zip.write(sheetXml(headers, rows).toByteArray())
            zip.closeEntry()
        }
    }

    private fun workbookXml(sheetName: String) = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="${xmlEscape(sheetName)}" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""

    private fun sheetXml(headers: List<String>, rows: List<List<String>>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")

        fun colLetter(index: Int): String {
            var i = index
            val sb2 = StringBuilder()
            while (i >= 0) {
                sb2.insert(0, ('A' + (i % 26)))
                i = i / 26 - 1
            }
            return sb2.toString()
        }

        fun rowXml(rowIndex: Int, values: List<String>): String {
            val cells = values.mapIndexed { i, v ->
                """<c r="${colLetter(i)}${rowIndex}" t="inlineStr"><is><t xml:space="preserve">${xmlEscape(v)}</t></is></c>"""
            }.joinToString("")
            return """<row r="$rowIndex">$cells</row>"""
        }

        sb.append(rowXml(1, headers))
        rows.forEachIndexed { idx, row -> sb.append(rowXml(idx + 2, row)) }

        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun xmlEscape(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private const val CONTENT_TYPES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""

    private const val RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private const val WORKBOOK_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""
}
