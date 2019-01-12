package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;
import org.itsallcode.openfasttrace.importer.SpecificationListBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventingSpecTreeProcessor extends Treeprocessor {
    private static final Logger LOG = LoggerFactory.getLogger(EventingSpecTreeProcessor.class);
    private SpecificationListBuilder specList = SpecificationListBuilder.create();


    @Override
    public Document process(Document document) {
        document.getBlocks().forEach(this::recurseDepthFirst);
        return document;
    }

    private void recurseDepthFirst(StructuralNode structuralNode) {
        LOG.debug(structuralNode.getSourceLocation() + ": processing {}", structuralNode);
        structuralNode.getBlocks().forEach(this::recurseDepthFirst);
    }
}
