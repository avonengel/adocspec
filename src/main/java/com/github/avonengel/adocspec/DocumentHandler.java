package com.github.avonengel.adocspec;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.itsallcode.openfasttrace.core.SpecificationItem;

import java.util.List;

public class DocumentHandler implements NodeHandler<List<SpecificationItem>> {
    @Override
    public NodeResult<List<SpecificationItem>> handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof Document) {
            Document document = (Document) node;
            document.getBlocks().forEach(StructuralNode::convert);
            return NodeResult.of(context.getSpecListBuilder().build());
        }
        return null;
    }
}
