package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> attributes = structuralNode.getAttributes();
        specBuilder.id("test", "test", 1);
        LOG.debug("node ID: {}", structuralNode.getId());
        LOG.debug("node attributes: {}", structuralNode.getAttributes());
        Object specID = structuralNode.getAttribute("specID");
        LOG.debug("specID attribute: {}", specID);
        LOG.debug("node title: {}", structuralNode.getTitle());
        LOG.debug("node context: {}", structuralNode.getContext());
        Object specIdAttributeValue = structuralNode.getAttribute("specID");
        if(specIdAttributeValue instanceof String) {
            specBuilder.id(SpecificationItemId.parseId((String) specIdAttributeValue));
        }

        specificationItems.add(specBuilder.build());
    }

    public List<SpecificationItem> getSpecObjects() {
        return Collections.unmodifiableList(specificationItems);
    }
}
