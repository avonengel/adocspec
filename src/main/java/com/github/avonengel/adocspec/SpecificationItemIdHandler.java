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
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.StructuralNode;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class SpecificationItemIdHandler implements BlockContentHandler {
    private static final String STDIN = "<stdin>";

    @Override
    public Optional<Object> handleNode(Block block, String content, ConversionContext context) {
        if (MdPattern.ID.getPattern().matcher(content).matches()) {
            // [impl->dsn~oft-equivalent.id~1]
            context.getSpecListBuilder().endSpecificationItem();
            context.getSpecListBuilder().beginSpecificationItem();
            context.getSpecListBuilder().setId(SpecificationItemId.parseId(content));
            // [impl->dsn~oft-equivalent.source-file-line~1]
            final Cursor sourceLocation = block.getSourceLocation();
            String fileLocation;
            if (!STDIN.equals(sourceLocation.getPath())) {
                final Path fullPath = Paths.get(sourceLocation.getDir(), sourceLocation.getPath());
                fileLocation = fullPath.toAbsolutePath().toString();
            } else {
                fileLocation = sourceLocation.getPath();
            }
            context.getSpecListBuilder().setLocation(fileLocation, sourceLocation.getLineNumber());
            context.setState(SpecificationConverter.State.SPEC);
            ContentNode parent = block.getParent();
            if (parent instanceof StructuralNode && ((StructuralNode) parent).getBlocks().indexOf(block) == 0) {
                // [impl->dsn~oft-equivalent.specification-item-title~1]
                context.getSpecListBuilder().setTitle(((StructuralNode) parent).getTitle());
            }
            return Optional.empty();
        }
        return null;
    }
}
