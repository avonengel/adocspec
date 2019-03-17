package com.github.avonengel.adocspec;

import org.asciidoctor.ast.*;
import org.asciidoctor.converter.AbstractConverter;
import org.itsallcode.openfasttrace.core.ItemStatus;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.itsallcode.openfasttrace.exporter.specobject.SpecobjectWriterExporterFactory;
import org.itsallcode.openfasttrace.importer.SpecificationListBuilder;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SpecificationConverter extends AbstractConverter<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(SpecificationConverter.class);

    enum State {
        START,
        SPEC,
        COVERS,
        RATIONALE,
        COMMENT,
        DESCRIPTION,
        DEPENDS,
    }

    private final BlockSpecListBuilder specListBuilder = new BlockSpecListBuilder(SpecificationListBuilder.create());
    private ConversionContext context = new ConversionContext(specListBuilder);
    private java.util.List<NodePipeFilter> pipeFilters = new LinkedList<>();

    {
        pipeFilters.add(new DocumentPipeFilter());
        pipeFilters.add(new SectionPipeFilter());
        pipeFilters.add(new PhraseConverter());
        pipeFilters.add(new ListPipeFilter());
        pipeFilters.add(new BlockConverter());
    }

    public SpecificationConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
        setOutfileSuffix(".xml");
    }

    @Override
    public Object convert(ContentNode node, String transform, Map<Object, Object> opts) {
        logConvertCall(node, transform, opts);
        for (NodePipeFilter pipeFilter : pipeFilters) {
            Object result = pipeFilter.handleNode(node, context);
            if (result != null) {
                return result;
            }
        }

        return "node type: " + node.getClass() + " node name: " + node.getNodeName() + "\n";
    }


    private void logConvertCall(ContentNode node, String transform, Map<Object, Object> opts) {
        if (node instanceof StructuralNode) {
            final Cursor sourceLocation = ((StructuralNode) node).getSourceLocation();
            LOG.info("{} convert {} ({}), {}, {}", sourceLocation, node, node.getNodeName(), transform, opts);
        } else {
            LOG.info("convert {} ({}), {}, {}", node, node.getNodeName(), transform, opts);
        }
    }

    @Override
    public void write(Object output, OutputStream out) throws IOException {
        final Stream<SpecificationItem> specStream = specListBuilder.build().stream();
        final SpecobjectWriterExporterFactory exporterFactory = new SpecobjectWriterExporterFactory();
        try (Writer w = new OutputStreamWriter(out, Charset.forName("UTF-8"))) {
            exporterFactory.createExporter(w, specStream).runExport();
        }
    }
}
