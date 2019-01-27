package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.converter.AbstractConverter;
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
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Stream;

public class SpecificationConverter extends AbstractConverter<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(SpecificationConverter.class);
    private final SpecificationListBuilder specListBuilder = SpecificationListBuilder.create();

    public SpecificationConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
    }

    @Override
    public Object convert(ContentNode node, String transform, Map<Object, Object> opts) {
        LOG.info("Asciidoctor converter: convert {} ({}), {}, {}", node, node.getNodeName(), transform, opts);
        if (node instanceof Document) {
            final Document document = (Document) node;
            document.getBlocks().forEach(StructuralNode::convert);

            return specListBuilder.build();
//        } else if (node instanceof StructuralNode) {
//            final StructuralNode structuralNode = (StructuralNode) node;
//            structuralNode.getBlocks().forEach(this::convertBlock);
        } else if (node instanceof PhraseNode) {
            final PhraseNode phrase = (PhraseNode) node;
            LOG.info("phrase target {}", phrase.getTarget());
            LOG.info("phrase text {}", phrase.getText());
            LOG.info("phrase context {}", phrase.getContext());
            LOG.info("phrase reftext {}", phrase.getReftext());
            LOG.info("phrase type {}", phrase.getType());
            LOG.info("phrase id {}", phrase.getId());
            LOG.info("phrase nodename {}", phrase.getNodeName());
            LOG.info("phrase role {}", phrase.getRoles());

            // this might work, right?
            /*
             1. convert phrases to whatever the OFT people do with markup in markdown-formatted content
                this results in more-or-less plaintext that contains no markup artifacts (HTML5 or DocBook tags)
             2. convert blocks just like the SpecTreeProcessor
             3. return XMl-representation of the specification items
             4. idea for later: return java object instead of XML-string, only this write-to-stream thing on request
             */
            return phrase.getText();
        } else if (node instanceof Block) {
            Block block = (Block) node;
            final String convertedBlock = block.getContent().toString();
            final Matcher matcher = MdPattern.ID.getPattern().matcher(convertedBlock);
            if(matcher.matches()) {
                specListBuilder.beginSpecificationItem();
                specListBuilder.setId(SpecificationItemId.parseId(convertedBlock));
            }
        }

        return "node type: " + node.getClass() + " node name: " + node.getNodeName() + "\n";
    }

    private String convertToSpecFormat() {
        final Stream<SpecificationItem> specStream = specListBuilder.build().stream();
        final SpecobjectWriterExporterFactory exporterFactory = new SpecobjectWriterExporterFactory();
        final StringWriter stringWriter = new StringWriter();
        exporterFactory.createExporter(stringWriter, specStream).runExport();
        return stringWriter.toString();
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
