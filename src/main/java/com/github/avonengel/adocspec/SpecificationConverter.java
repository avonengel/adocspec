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
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SpecificationConverter extends AbstractConverter<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(SpecificationConverter.class);
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile(MdPattern.DESCRIPTION.getPattern().pattern() + "(.*)", Pattern.DOTALL);
    private static final Pattern RATIONALE_PATTERN = Pattern.compile(MdPattern.RATIONALE.getPattern().pattern() + "(.*)", Pattern.DOTALL);
    private static final Pattern COMMENT_PATTERN = Pattern.compile(MdPattern.COMMENT.getPattern().pattern() + "(.*)", Pattern.DOTALL);
    private static final Pattern FORWARD_PATTERN = Pattern.compile("(?<forwardingType>[a-zA-Z]+)"
            + "\\s*"
            + "&#8594;"
            + "\\s*"
            + "(?<needsList>[a-zA-Z]+"
            + "(?:,\\s*"
            + "[a-zA-Z]+"
            + ")*)"
            + "\\s*"
            + ":"
            + "\\s*(?<coveredId>"
            + SpecificationItemId.ID_PATTERN
            + ")");
    private static final String STDIN = "<stdin>";

    private enum State {
        START,
        SPEC,
        COVERS,
        RATIONALE,
        COMMENT,
        DESCRIPTION,
        DEPENDS,
    }

    private final SpecificationListBuilder specListBuilder = SpecificationListBuilder.create();
    private State state = State.START;
    private String lastTitle = null;

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
            // [impl->dsn~oft-equivalent.specification-item-title~1]
            lastTitle = section.getTitle();
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

            return phrase.getText();
        } else if (node instanceof Block) {
            Block block = (Block) node;
            if ("thematic_break".equals(node.getNodeName())) {
                lastTitle = null; // TODO: refactor so this cannot get lost
                return null;
            }
            final String convertedBlock = block.getContent().toString();

            final Matcher forwardMatcher = FORWARD_PATTERN.matcher(convertedBlock);
            final Matcher needsMatcher = MdPattern.NEEDS_INT.getPattern().matcher(convertedBlock);
            final Matcher tagsMatcher = MdPattern.TAGS_INT.getPattern().matcher(convertedBlock);
            final Matcher descriptionMatcher = DESCRIPTION_PATTERN.matcher(convertedBlock);
            final Matcher rationaleMatcher = RATIONALE_PATTERN.matcher(convertedBlock);
            final Matcher commentMatcher = COMMENT_PATTERN.matcher(convertedBlock);
            final Matcher coversMatcher = MdPattern.COVERS.getPattern().matcher(convertedBlock);
            final Matcher dependsMatcher = MdPattern.DEPENDS.getPattern().matcher(convertedBlock);
            final Matcher statusMatcher = MdPattern.STATUS.getPattern().matcher(convertedBlock);

            if (inExample(block)) {
                return convertedBlock;
            } else if ("example".equals(block.getNodeName())) {
                appendTextBlock(convertedBlock);
            } else if (MdPattern.ID.getPattern().matcher(convertedBlock).matches()) {
                // [impl->dsn~oft-equivalent.id~1]
                specListBuilder.endSpecificationItem();
                specListBuilder.beginSpecificationItem();
                specListBuilder.setId(SpecificationItemId.parseId(convertedBlock));
                // [impl->dsn~oft-equivalent.source-file-line~1]
                final Cursor sourceLocation = block.getSourceLocation();
                String fileLocation;
                if (!STDIN.equals(sourceLocation.getPath())) {
                    final Path fullPath = Paths.get(sourceLocation.getDir(), sourceLocation.getPath());
                    fileLocation = fullPath.toAbsolutePath().toString();
                } else {
                    fileLocation = sourceLocation.getPath();
                }
                specListBuilder.setLocation(fileLocation, sourceLocation.getLineNumber());
                state = State.SPEC;
                if (lastTitle != null) {
                    specListBuilder.setTitle(lastTitle);
                }
            } else if (forwardMatcher.matches()) {
                // [impl->dsn~oft-equivalent.forwarding_needed_coverage~1]
                specListBuilder.endSpecificationItem();
                specListBuilder.beginSpecificationItem();
                specListBuilder.setForwards(true);
                SpecificationItemId coveredId = SpecificationItemId.parseId(forwardMatcher.group("coveredId"));
                specListBuilder.addCoveredId(coveredId);
                specListBuilder.setId(SpecificationItemId.createId(forwardMatcher.group("forwardingType"), coveredId.getName(), coveredId.getRevision()));
                String[] needsTypes = forwardMatcher.group("needsList").split(",");
                for (String needsType : needsTypes) {
                    specListBuilder.addNeededArtifactType(needsType.trim());
                }
                specListBuilder.endSpecificationItem();
                state = State.START;
            } else if (needsMatcher.matches()) {
                // [impl->dsn~oft-equivalent.needs~1]
                for (final String artifactType : needsMatcher.group(1).split(",\\s*")) {
                    specListBuilder.addNeededArtifactType(artifactType);
                }
            } else if (tagsMatcher.matches()) {
                // [impl->dsn~oft-equivalent.tags~1]
                for (final String tag : tagsMatcher.group(1).split(",\\s*")) {
                    specListBuilder.addTag(tag);
                }
            } else if (descriptionMatcher.matches()) {
                // [impl->dsn~oft-equivalent.description~2]
                state = State.DESCRIPTION;
                specListBuilder.appendDescription(descriptionMatcher.group(1));
            } else if (commentMatcher.matches()) {
                // [impl->dsn~oft-equivalent.comment~1]
                state = State.COMMENT;
                specListBuilder.appendComment(commentMatcher.group(1));
            } else if (rationaleMatcher.matches()) {
                // [impl->dsn~oft-equivalent.rationale~1]
                state = State.RATIONALE;
                specListBuilder.appendRationale(rationaleMatcher.group(1));
            } else if (coversMatcher.matches()) {
                state = State.COVERS;
            } else if (dependsMatcher.matches()) {
                state = State.DEPENDS;
            } else if (statusMatcher.matches()) {
                // [impl->dsn~oft-equivalent.status~1]
                specListBuilder.setStatus(ItemStatus.parseString(statusMatcher.group(1)));
            } else {
                appendTextBlock(convertedBlock);
            }
        } else if (node instanceof List) {
            List list = (List) node;
            if (state == State.COVERS) {
                // [impl->dsn~oft-equivalent.covers~1]
                final Consumer<SpecificationItemId> idConsumer = specListBuilder::addCoveredId;
                readSpecificationItemIdList(list, idConsumer);
            } else if (state == State.DEPENDS) {
                // [impl->dsn~oft-equivalent.depends-list~1]
                readSpecificationItemIdList(list, specListBuilder::addDependsOnId);
            }
            lastTitle = null; // TODO: refactor so this cannot get lost
        } else {
            lastTitle = null; // TODO: refactor so this cannot get lost
        }

        return "node type: " + node.getClass() + " node name: " + node.getNodeName() + "\n";
    }

    private boolean inExample(Block block) {
        ContentNode current = block.getParent();
        while (current != null) {
            if ("example".equals(current.getNodeName())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private void appendTextBlock(String convertedBlock) {
        if (state == State.SPEC || state == State.DESCRIPTION) {
            // [impl->dsn~oft-equivalent.description~2]
            specListBuilder.appendDescription(convertedBlock);
        } else if (state == State.COMMENT) {
            specListBuilder.appendComment(convertedBlock);
        } else if (state == State.RATIONALE) {
            specListBuilder.appendRationale(convertedBlock);
        }
    }

    private void readSpecificationItemIdList(List list, Consumer<SpecificationItemId> idConsumer) {
        for (StructuralNode item : list.getItems()) {
            if (item instanceof ListItem) {
                ListItem listItem = (ListItem) item;
                String itemContent = listItem.getText();
                final Matcher idMatcher = SpecificationItemId.ID_PATTERN.matcher(itemContent);
                if (idMatcher.matches()) {
                    idConsumer.accept(SpecificationItemId.parseId(itemContent));
                }
            }
        }
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
