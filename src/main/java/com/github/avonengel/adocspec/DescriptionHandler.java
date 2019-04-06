package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DescriptionHandler implements BlockContentHandler {
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile(MdPattern.DESCRIPTION.getPattern().pattern() + "(.*)", Pattern.DOTALL);

    @Override
    public Optional<Object> handleNode(Block block, String content, ConversionContext context) {
        final Matcher descriptionMatcher = DESCRIPTION_PATTERN.matcher(content);
        if (descriptionMatcher.matches()) {
            // [impl->dsn~oft-equivalent.description~2]
            context.setState(SpecificationConverter.State.DESCRIPTION);
            appendTextBlock(descriptionMatcher.group(1), context);
            return Optional.empty();
        }
        return null;
    }
}
