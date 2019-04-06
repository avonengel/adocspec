package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RationaleHandler implements BlockContentHandler {
    private static final Pattern RATIONALE_PATTERN = Pattern.compile(MdPattern.RATIONALE.getPattern().pattern() + "(.*)", Pattern.DOTALL);

    @Override
    public Optional<Object> handleNode(Block block, String content, ConversionContext context) {
        final Matcher rationaleMatcher = RATIONALE_PATTERN.matcher(content);
        if (rationaleMatcher.matches()) {
            // [impl->dsn~oft-equivalent.rationale~1]
            context.setState(SpecificationConverter.State.RATIONALE);
            appendTextBlock(rationaleMatcher.group(1), context);
            return Optional.empty();
        }
        return null;
    }
}
