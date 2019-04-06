package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.itsallcode.openfasttrace.core.ItemStatus;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;

import java.util.Optional;
import java.util.regex.Matcher;

public class BlockConverter implements NodeHandler {

    @Override
    public Optional<Object> handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof Block) {
            Block block = (Block) node;
            final String convertedBlock = block.getContent().toString();

            final Matcher statusMatcher = MdPattern.STATUS.getPattern().matcher(convertedBlock);


            if (statusMatcher.matches()) {
                // [impl->dsn~oft-equivalent.status~1]
                context.getSpecListBuilder().setStatus(ItemStatus.parseString(statusMatcher.group(1)));
            } else {
                appendTextBlock(convertedBlock, context);
            }
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
