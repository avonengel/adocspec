package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;

import java.util.Arrays;
import java.util.List;

public class BlockHandler implements NodeHandler {
    private List<NodeContentHandler> nodeContentHandlers;

    public BlockHandler(NodeContentHandler... nodeContentHandlers) {
        this.nodeContentHandlers = Arrays.asList(nodeContentHandlers);
    }

    @Override
    public NodeResult handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof Block) {
            Block block = (Block) node;
            final String content = block.getContent().toString();
            for (NodeContentHandler contentHandler : nodeContentHandlers) {
                final NodeResult<?> result = contentHandler.handleNode(node, content, context);
                if (result != null && !result.shouldContinue()) {
                    return result;
                }
            }

        }
        return null;
    }
}
