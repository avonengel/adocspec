/**
 * adocspec - AsciidoctorJ extension to specify requirements via OpenFastTrace
 * Copyright (C) 2019 Axel von Engel <a.vonengel@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
