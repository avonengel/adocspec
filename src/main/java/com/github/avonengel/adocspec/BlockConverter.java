package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.itsallcode.openfasttrace.core.ItemStatus;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockConverter implements NodeHandler {
    private static final Pattern RATIONALE_PATTERN = Pattern.compile(MdPattern.RATIONALE.getPattern().pattern() + "(.*)", Pattern.DOTALL);
    private static final Pattern COMMENT_PATTERN = Pattern.compile(MdPattern.COMMENT.getPattern().pattern() + "(.*)", Pattern.DOTALL);

    @Override
    public Optional<Object> handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof Block) {
            Block block = (Block) node;
            final String convertedBlock = block.getContent().toString();

            final Matcher rationaleMatcher = RATIONALE_PATTERN.matcher(convertedBlock);
            final Matcher commentMatcher = COMMENT_PATTERN.matcher(convertedBlock);
            final Matcher coversMatcher = MdPattern.COVERS.getPattern().matcher(convertedBlock);
            final Matcher dependsMatcher = MdPattern.DEPENDS.getPattern().matcher(convertedBlock);
            final Matcher statusMatcher = MdPattern.STATUS.getPattern().matcher(convertedBlock);


            if (commentMatcher.matches()) {
                // [impl->dsn~oft-equivalent.comment~1]
                context.setState(SpecificationConverter.State.COMMENT);
                appendTextBlock(commentMatcher.group(1), context);
            } else if (rationaleMatcher.matches()) {
                // [impl->dsn~oft-equivalent.rationale~1]
                context.setState(SpecificationConverter.State.RATIONALE);
                appendTextBlock(rationaleMatcher.group(1), context);
            } else if (coversMatcher.matches()) {
                context.setState(SpecificationConverter.State.COVERS);
            } else if (dependsMatcher.matches()) {
                context.setState(SpecificationConverter.State.DEPENDS);
            } else if (statusMatcher.matches()) {
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
