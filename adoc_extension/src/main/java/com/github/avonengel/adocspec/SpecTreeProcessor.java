package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class SpecTreeProcessor extends Treeprocessor {

    private static final Logger LOG = LoggerFactory.getLogger(SpecTreeProcessor.class);
    private List<SpecificationItem> specificationItems = new LinkedList<>();

    @Override
    public Document process(Document document) {
        LOG.debug("Examining document: {}", document.getDoctitle());
        searchDocumentForSpecSections(document);
        return document;
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
        LOG.debug("node ID: {}", structuralNode.getId());
        LOG.debug("node attributes: {}", structuralNode.getAttributes());
        LOG.debug("node title: {}", structuralNode.getTitle());
        LOG.debug("node context: {}", structuralNode.getContext());
        Object specID = structuralNode.getAttribute("specID");
        LOG.debug("specID attribute: {}", specID);
        if (specID instanceof String) {
            specBuilder.id(SpecificationItemId.parseId((String) specID));
        }

        parseContent(structuralNode, specBuilder);
        specificationItems.add(specBuilder.build());
    }

    private void parseContent(StructuralNode structuralNode, SpecificationItem.Builder specBuilder) {
        StringBuilder description = new StringBuilder();

        for (StructuralNode block : structuralNode.getBlocks()) {
            Object content = block.getContent();
            if (!(content instanceof String)) {
                LOG.warn("Found non-string content ({}): {}", content.getClass(), content);
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

        specBuilder.description(description.toString());
    }

    public List<SpecificationItem> getSpecObjects() {
        return Collections.unmodifiableList(specificationItems);
    }
}
