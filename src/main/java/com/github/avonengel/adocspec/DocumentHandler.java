package com.github.avonengel.adocspec;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import java.util.Optional;

public class DocumentHandler implements NodeHandler {
    @Override
    public Optional<Object> handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof Document) {
            Document document = (Document) node;
            document.getBlocks().forEach(StructuralNode::convert);
            return Optional.of(context.getSpecListBuilder().build());
        }
        return null;
    }
}
