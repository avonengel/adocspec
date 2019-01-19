package org.itsallcode.openfasttrace.exporter.specobject;

import org.itsallcode.openfasttrace.core.Newline;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.exporter.Exporter;

import java.io.Writer;
import java.util.stream.Stream;

public class SpecobjectWriterExporterFactory extends SpecobjectExporterFactory {
    public Exporter createExporter(Writer writer, Stream<SpecificationItem> itemStream) {
        return super.createExporter(writer, itemStream, Newline.UNIX);
    }
}
