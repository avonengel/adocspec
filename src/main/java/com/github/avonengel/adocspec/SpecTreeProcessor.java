package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;
import org.itsallcode.openfasttrace.ExportSettings;
import org.itsallcode.openfasttrace.Oft;
import org.itsallcode.openfasttrace.core.Newline;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecTreeProcessor extends Treeprocessor {

    private static final Logger LOG = LoggerFactory.getLogger(SpecTreeProcessor.class);
    private static final Pattern ID_PATTERN = Pattern.compile("`?(?:\\$\\$|\\+{1,3}|pass:[cqarmp,]*\\[)(?<specId>"
            + SpecificationItemId.ID_PATTERN
            + ")(?:\\$\\$|\\+{1,3}|])`?");
    private List<SpecificationItem> specificationItems = new LinkedList<>();

    @Override
    public Document process(Document document) {
        LOG.debug("Examining document: {}", document.getDoctitle());
        searchDocumentForSpecSections(document);
        export(document);
        return document;
    }

    private void export(Document document) {
        Path exportPath = getExportPath(document);
        if (exportPath != null) {
            LOG.info("Exporting to {}", exportPath);
            Oft.create().exportToPath(specificationItems, exportPath);
        } else {
            LOG.info("Not exporting specification items, as no export path is set.");
        }
    }

    private Path getExportPath(Document document) {
        final Object specfile = document.getAttribute("specfile");
        final Object outdir = document.getAttribute("outdir");
        LOG.debug("specfile: {}", specfile);
        if (specfile != null && !specfile.toString().isEmpty()) {
            return Paths.get(specfile.toString());
        } else if (outdir instanceof String && !((String) outdir).isEmpty()) {
            Object docname = document.getAttribute("docname");
            return Paths.get(outdir.toString()).resolve(docname.toString() + ".reqm");
        }
        return null;
    }

    private void searchDocumentForSpecSections(Document document) {
        if (document != null) {
            Map<Object, Object> findBy = new HashMap<>();
            findBy.put("role", "spec");
            List<StructuralNode> specNodes = document.findBy(findBy);
            specNodes.forEach(this::processSpecNode);
        }
    }

    private void processSpecNode(StructuralNode structuralNode) {
        SpecificationItem.Builder specBuilder = SpecificationItem.builder();
        LOG.debug(structuralNode.getSourceLocation() + ": node ID = {}", structuralNode.getId());
        LOG.debug(structuralNode.getSourceLocation() + ": node attributes = {}", structuralNode.getAttributes());
        LOG.debug(structuralNode.getSourceLocation() + ": node title = {}", structuralNode.getTitle());
        LOG.debug(structuralNode.getSourceLocation() + ": node context = {}", structuralNode.getContext());
        Object specID = structuralNode.getAttribute("specID");
        LOG.debug(structuralNode.getSourceLocation() + ": specID attribute = {}", specID);
        if (specID instanceof String) {
            specBuilder.id(SpecificationItemId.parseId((String) specID));
        }

        parseContent(structuralNode, specBuilder);
        specificationItems.add(specBuilder.build());
    }

    private void parseContent(StructuralNode structuralNode, SpecificationItem.Builder specBuilder) {
        StringBuilder description = new StringBuilder();

        for (StructuralNode block : structuralNode.getBlocks()) {
            if (block.getRoles().contains("covers")) {
                if (block instanceof org.asciidoctor.ast.List) {
                    org.asciidoctor.ast.List list = (org.asciidoctor.ast.List) block;
                    list.getItems().forEach((StructuralNode item) -> {
                        if (item instanceof ListItem) {
                            final ListItem listItem = (ListItem) item;
                            SpecificationItemId specId = extractSpecIdFromListItem(listItem);
                            if (specId != null) {
                                specBuilder.addCoveredId(specId);
                            } else {
                                throw new IllegalFormatException(item.getSourceLocation() + ": Encountered ListItem that is not a SpecificationItemId: " + listItem.getSource());
                            }
                        } else {
                            throw new IllegalFormatException(item.getSourceLocation() + ": Encountered non-ListItem in covers List: " + item.getContent());
                        }
                    });
                } else {
                    throw new IllegalFormatException(block.getSourceLocation() + ": Encountered non-List block with role .covers");
                }
            } else {
                Object content = block.getContent();
                if (!(content instanceof String)) {
                    LOG.warn(block.getSourceLocation() + ": Found non-string content ({}): {}", content.getClass(), content);
                    continue;
                }
                String stringContent = (String) content;
                final Matcher needsMatcher = MdPattern.NEEDS_INT.getPattern().matcher(stringContent);
                if (needsMatcher.matches()) {
                    for (String needs : needsMatcher.group(1).split(",\\s*")) {
                        specBuilder.addNeedsArtifactType(needs);
                    }
                } else {
                    description.append(content.toString());
                }
            }
        }

        specBuilder.description(description.toString());
    }

    private SpecificationItemId extractSpecIdFromListItem(ListItem listItem) {
        Matcher sourceMatcher = ID_PATTERN.matcher(listItem.getSource());
        if (sourceMatcher.matches()) {
            return SpecificationItemId.parseId(sourceMatcher.group("specId"));
        }
        return null;
    }

    List<SpecificationItem> getSpecObjects() {
        return Collections.unmodifiableList(specificationItems);
    }
}
