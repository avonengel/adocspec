package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BlockHandler implements NodeHandler {
    private final List<BlockContentHandler> blockContentHandlers;

    public BlockHandler(BlockContentHandler... blockContentHandlers) {
        this.blockContentHandlers = Arrays.asList(blockContentHandlers);
    }

    @Override
    public Optional<Object> handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof Block) {
            Block block = (Block) node;
            final String content = block.getContent().toString();
            for (BlockContentHandler contentHandler : blockContentHandlers) {
                final Optional<Object> result = contentHandler.handleNode(block, content, context);
                if (result != null) {
                    return result;
                }
            }

        }
        return null;
    }
}
