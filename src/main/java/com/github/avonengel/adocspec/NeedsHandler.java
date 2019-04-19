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
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;

import java.util.Optional;
import java.util.regex.Matcher;

public class NeedsHandler implements BlockContentHandler {
    @Override
    public Optional<Object> handleNode(Block block, String content, ConversionContext context) {
        final Matcher needsMatcher = MdPattern.NEEDS_INT.getPattern().matcher(content);
        if (needsMatcher.matches()) {
            // [impl->dsn~oft-equivalent.needs~1]
            for (final String artifactType : needsMatcher.group(1).split(",\\s*")) {
                context.getSpecListBuilder().addNeededArtifactType(artifactType);
            }

            return Optional.empty();
        }
        return null;
    }
}
