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

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SectionHandler implements NodeHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SectionHandler.class);

    @Override
    public Optional<Object> handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof Section) {
            final Section section = (Section) node;
            LOG.info("Processing section {}", section.getTitle());
            context.getSpecListBuilder().endSpecificationItem();
            section.getBlocks().forEach(StructuralNode::convert);
            context.getSpecListBuilder().endSpecificationItem();
            context.setState(SpecificationConverter.State.START);
            return Optional.empty();
        }
        return null;
    }
}
