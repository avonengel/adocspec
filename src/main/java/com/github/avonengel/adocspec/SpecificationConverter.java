package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.List;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.Section;
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
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SpecificationConverter extends AbstractConverter<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(SpecificationConverter.class);
    private static final Pattern RATIONALE_PATTERN = Pattern.compile(MdPattern.RATIONALE.getPattern().pattern() + "(.*)", Pattern.DOTALL);

    private enum State {
        START,
        SPEC,
        COVERS,
    }

    private final SpecificationListBuilder specListBuilder = SpecificationListBuilder.create();
    private State state = State.START;

    public SpecificationConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
        setOutfileSuffix(".xml");
    }

    @Override
    public Object convert(ContentNode node, String transform, Map<Object, Object> opts) {
        logConvertCall(node, transform, opts);
        if (node instanceof Document) {
            final Document document = (Document) node;
            document.getBlocks().forEach(StructuralNode::convert);

            return specListBuilder.build();
        } else if (node instanceof Section) {
            final Section section = (Section) node;
            LOG.info("Processing section {}", section.getTitle());
            section.getBlocks().forEach(StructuralNode::convert);
            specListBuilder.endSpecificationItem();
            state = State.START;
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
            if (MdPattern.ID.getPattern().matcher(convertedBlock).matches()) {
                specListBuilder.beginSpecificationItem();
                specListBuilder.setId(SpecificationItemId.parseId(convertedBlock));
                state = State.SPEC;
            } else {
                final Matcher needsMatcher = MdPattern.NEEDS_INT.getPattern().matcher(convertedBlock);
                final Matcher rationaleMatcher = RATIONALE_PATTERN.matcher(convertedBlock);
                final Matcher coversMatcher = MdPattern.COVERS.getPattern().matcher(convertedBlock);
                if (needsMatcher.matches()) {
                    for (final String artifactType : needsMatcher.group(1).split(",\\s*")) {
                        specListBuilder.addNeededArtifactType(artifactType);
                    }
                } else if (rationaleMatcher.matches()) {
                    specListBuilder.appendRationale(rationaleMatcher.group(1));
                } else if (coversMatcher.matches()) {
                    state = State.COVERS;
                } else if (state == State.SPEC) {
                    specListBuilder.appendDescription(convertedBlock);
                }
            }
        } else if (node instanceof List) {
            List list = (List) node;
            if (state == State.COVERS) {
                for (StructuralNode item : list.getItems()) {
                    if (item instanceof ListItem) {
                        ListItem listItem = (ListItem) item;
                        String itemContent = listItem.getText();
                        final Matcher idMatcher = SpecificationItemId.ID_PATTERN.matcher(itemContent);
                        if (idMatcher.matches()) {
                            specListBuilder.addCoveredId(SpecificationItemId.parseId(itemContent));
                        }
                    }
                }
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
