package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;
import org.itsallcode.openfasttrace.core.SpecificationItem;
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
        LOG.info("Examining document: {}", document.getDoctitle());
        searchBlocksForSpecSections(document);
        Map<String, Object> config = getConfig();
        return document;
    }

    private void searchBlocksForSpecSections(Document document) {
        if (document != null) {
            Map<Object, Object> findBy = new HashMap<>();
            List<StructuralNode> specNodes = document.findBy(findBy);
            specNodes.forEach(this::processSpecNode);
        }
    }

    private void processSpecNode(StructuralNode structuralNode) {
        SpecificationItem.Builder specBuilder = SpecificationItem.builder();
        specBuilder.id("test", "test", 1);
        specificationItems.add(specBuilder.build());
    }

    public List<SpecificationItem> getSpecObjects() {
        return Collections.unmodifiableList(specificationItems);
    }
}
