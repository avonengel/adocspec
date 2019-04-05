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
