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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BlockHandler implements NodeHandler {
    private final List<BlockContentHandler> blockContentHandlers;

    public BlockHandler(BlockContentHandler... blockContentHandlers) {
        this.blockContentHandlers = Arrays.asList(blockContentHandlers);
    }

    @Override
    public Optional<Object> handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof Block) {
            Block block = (Block) node;
            final String content = block.getContent().toString();
            for (BlockContentHandler contentHandler : blockContentHandlers) {
                final Optional<Object> result = contentHandler.handleNode(block, content, context);
                if (result != null) {
                    return result;
                }
            }

        }
        return null;
    }
}
