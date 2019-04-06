package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;

import java.util.Optional;

public class BlockConverter implements NodeHandler {

    @Override
    public Optional<Object> handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof Block) {
            Block block = (Block) node;
            final String convertedBlock = block.getContent().toString();

            appendTextBlock(convertedBlock, context);
        }
        return null;
    }

    private void appendTextBlock(String convertedBlock, ConversionContext context) {
        if (convertedBlock.trim().isEmpty()) {
            return;
        }
        if (context.getState() == SpecificationConverter.State.SPEC || context.getState() == SpecificationConverter.State.DESCRIPTION) {
            // [impl->dsn~oft-equivalent.description~2]
            context.getSpecListBuilder().appendDescription(convertedBlock);
        } else if (context.getState() == SpecificationConverter.State.COMMENT) {
            context.getSpecListBuilder().appendComment(convertedBlock);
        } else if (context.getState() == SpecificationConverter.State.RATIONALE) {
            context.getSpecListBuilder().appendRationale(convertedBlock);
        }
    }

}
