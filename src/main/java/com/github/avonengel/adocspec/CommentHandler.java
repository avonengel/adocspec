package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentHandler implements BlockContentHandler {
    private static final Pattern COMMENT_PATTERN = Pattern.compile(MdPattern.COMMENT.getPattern().pattern() + "(.*)", Pattern.DOTALL);

    @Override
    public Optional<Object> handleNode(Block block, String content, ConversionContext context) {
        final Matcher commentMatcher = COMMENT_PATTERN.matcher(content);
        if (commentMatcher.matches()) {
            // [impl->dsn~oft-equivalent.comment~1]
            context.setState(SpecificationConverter.State.COMMENT);
            appendTextBlock(commentMatcher.group(1), context);
            return Optional.empty();
        }
        return null;
    }
}
