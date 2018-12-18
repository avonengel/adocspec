package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SpecTreeProcessor extends Treeprocessor {

    Logger log = Logger.getLogger(SpecTreeProcessor.class.getName());

    @Override
    public Document process(Document document) {
//        System.out.println("Examining document: " + document);
        log.info("Examining document: " + document.getDoctitle());
        searchBlocksForSpecSections(document.getBlocks(), 0);
        Map<String, Object> config = getConfig();
        return document;
    }

    private void searchBlocksForSpecSections(List<StructuralNode> blocks, int depth) {
        if (blocks != null) {
            blocks.forEach(node -> reportBlocksDepthFirst(node, depth));
        }
    }

    private void reportBlocksDepthFirst(StructuralNode structuralNode, int depth) {
//        System.out.println("examining Node:" + structuralNode);
        log.info("examining Node caption:" + structuralNode.getCaption());
        log.info("examining Node title:" + structuralNode.getTitle());
        log.info("roles: " + structuralNode.getRoles());
        log.info("style: " + structuralNode.getStyle());
        searchBlocksForSpecSections(structuralNode.getBlocks(), depth + 1);
    }
}
