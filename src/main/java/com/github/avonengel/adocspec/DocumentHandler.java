package com.github.avonengel.adocspec;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

public class DocumentHandler implements NodeHandler {
    @Override
    public Object handleNode(ContentNode node, ConversionContext context) {
        if(node instanceof Document) {
            Document document = (Document) node;
            document.getBlocks().forEach(StructuralNode::convert);
            return context.getSpecListBuilder().build();
        }
        return null;
    }
}
