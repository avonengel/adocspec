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
import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.converter.AbstractConverter;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.exporter.specobject.SpecobjectWriterExporterFactory;
import org.itsallcode.openfasttrace.importer.SpecificationListBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class SpecificationConverter extends AbstractConverter<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(SpecificationConverter.class);
    private final BlockSpecListBuilder specListBuilder = new BlockSpecListBuilder(SpecificationListBuilder.create());
    private final ConversionContext context = new ConversionContext(specListBuilder);
    private final List<NodeHandler> handlers;

    public SpecificationConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
        setOutfileSuffix(".xml");
        this.handlers = buildHandlerList();
    }

    private List<NodeHandler> buildHandlerList() {
        final List<NodeHandler> handlers = new LinkedList<>();
        final CoversHandler coversHandler = new CoversHandler();
        final DependsHandler dependsHandler = new DependsHandler();
        handlers.add(new DocumentHandler());
        handlers.add(new SectionHandler());
        handlers.add(new PhraseConverter());
        handlers.add(coversHandler);
        handlers.add(dependsHandler);
        handlers.add(new BlockHandler(
                new ExampleHandler(),
                new SpecificationItemIdHandler(),
                new ForwardHandler(),
                new NeedsHandler(),
                new TagsHandler(),
                new DescriptionHandler(),
                new CommentHandler(),
                new RationaleHandler(),
                coversHandler,
                dependsHandler,
                new StatusHandler(),
                new UnqualifiedBlockHandler()
        ));
        return handlers;
    }

    @Override
    public Object convert(ContentNode node, String transform, Map<Object, Object> opts) {
        logConvertCall(node, transform, opts);
        for (NodeHandler handler : handlers) {
            Optional<Object> result = handler.handleNode(node, context);
            if (result != null) {
                return result.orElse(null);
            }
        }

        return "node type: " + node.getClass() + " node name: " + node.getNodeName() + "\n";
    }

    private void logConvertCall(ContentNode node, String transform, Map<Object, Object> opts) {
        if (node instanceof StructuralNode) {
            final Cursor sourceLocation = ((StructuralNode) node).getSourceLocation();
            LOG.info("{} convert {} ({}), {}, {}", sourceLocation, node, node.getNodeName(), transform, opts);
        } else {
            LOG.info("convert {} ({}), {}, {}", node, node.getNodeName(), transform, opts);
        }
    }

    @Override
    public void write(Object output, OutputStream out) throws IOException {
        final Stream<SpecificationItem> specStream = specListBuilder.build().stream();
        final SpecobjectWriterExporterFactory exporterFactory = new SpecobjectWriterExporterFactory();
        try (Writer w = new OutputStreamWriter(out, Charset.forName("UTF-8"))) {
            exporterFactory.createExporter(w, specStream).runExport();
        }
    }

    enum State {
        START,
        SPEC,
        COVERS,
        RATIONALE,
        COMMENT,
        DESCRIPTION,
        DEPENDS,
    }
}
