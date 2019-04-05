package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.itsallcode.openfasttrace.core.SpecificationItemId;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForwardHandler implements BlockContentHandler {
    private static final Pattern FORWARD_PATTERN = Pattern.compile("(?<forwardingType>[a-zA-Z]+)"
            + "\\s*"
            + "&#8594;"
            + "\\s*"
            + "(?<needsList>[a-zA-Z]+"
            + "(?:,\\s*"
            + "[a-zA-Z]+"
            + ")*)"
            + "\\s*"
            + ":"
            + "\\s*(?<coveredId>"
            + SpecificationItemId.ID_PATTERN
            + ")");

    @Override
    public Optional<Object> handleNode(Block block, String content, ConversionContext context) {
        final Matcher forwardMatcher = FORWARD_PATTERN.matcher(content);

        if (forwardMatcher.matches()) {
            // [impl->dsn~oft-equivalent.forwarding_needed_coverage~1]
            context.getSpecListBuilder().endSpecificationItem();
            context.getSpecListBuilder().beginSpecificationItem();
            context.getSpecListBuilder().setForwards(true);
            SpecificationItemId coveredId = SpecificationItemId.parseId(forwardMatcher.group("coveredId"));
            context.getSpecListBuilder().addCoveredId(coveredId);
            context.getSpecListBuilder().setId(SpecificationItemId.createId(forwardMatcher.group("forwardingType"), coveredId.getName(), coveredId.getRevision()));
            String[] needsTypes = forwardMatcher.group("needsList").split(",");
            for (String needsType : needsTypes) {
                context.getSpecListBuilder().addNeededArtifactType(needsType.trim());
            }
            context.getSpecListBuilder().endSpecificationItem();
            context.setState(SpecificationConverter.State.START);
            return Optional.empty();
        }
        return null;
    }
}
