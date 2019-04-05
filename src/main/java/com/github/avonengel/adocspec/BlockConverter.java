package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.StructuralNode;
import org.itsallcode.openfasttrace.core.ItemStatus;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.itsallcode.openfasttrace.importer.markdown.MdPattern;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockConverter implements NodeHandler {
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile(MdPattern.DESCRIPTION.getPattern().pattern() + "(.*)", Pattern.DOTALL);
    private static final Pattern RATIONALE_PATTERN = Pattern.compile(MdPattern.RATIONALE.getPattern().pattern() + "(.*)", Pattern.DOTALL);
    private static final Pattern COMMENT_PATTERN = Pattern.compile(MdPattern.COMMENT.getPattern().pattern() + "(.*)", Pattern.DOTALL);
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
    private static final String STDIN = "<stdin>";

    @Override
    public Object handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof Block) {
            Block block = (Block) node;
            final String convertedBlock = block.getContent().toString();

            final Matcher forwardMatcher = FORWARD_PATTERN.matcher(convertedBlock);
            final Matcher needsMatcher = MdPattern.NEEDS_INT.getPattern().matcher(convertedBlock);
            final Matcher tagsMatcher = MdPattern.TAGS_INT.getPattern().matcher(convertedBlock);
            final Matcher descriptionMatcher = DESCRIPTION_PATTERN.matcher(convertedBlock);
            final Matcher rationaleMatcher = RATIONALE_PATTERN.matcher(convertedBlock);
            final Matcher commentMatcher = COMMENT_PATTERN.matcher(convertedBlock);
            final Matcher coversMatcher = MdPattern.COVERS.getPattern().matcher(convertedBlock);
            final Matcher dependsMatcher = MdPattern.DEPENDS.getPattern().matcher(convertedBlock);
            final Matcher statusMatcher = MdPattern.STATUS.getPattern().matcher(convertedBlock);

            if (MdPattern.ID.getPattern().matcher(convertedBlock).matches()) {
                // [impl->dsn~oft-equivalent.id~1]
                context.getSpecListBuilder().endSpecificationItem();
                context.getSpecListBuilder().beginSpecificationItem();
                context.getSpecListBuilder().setId(SpecificationItemId.parseId(convertedBlock));
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
            } else if (forwardMatcher.matches()) {
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
            } else if (needsMatcher.matches()) {
                // [impl->dsn~oft-equivalent.needs~1]
                for (final String artifactType : needsMatcher.group(1).split(",\\s*")) {
                    context.getSpecListBuilder().addNeededArtifactType(artifactType);
                }
            } else if (tagsMatcher.matches()) {
                // [impl->dsn~oft-equivalent.tags~1]
                for (final String tag : tagsMatcher.group(1).split(",\\s*")) {
                    context.getSpecListBuilder().addTag(tag);
                }
            } else if (descriptionMatcher.matches()) {
                // [impl->dsn~oft-equivalent.description~2]
                context.setState(SpecificationConverter.State.DESCRIPTION);
                appendTextBlock(descriptionMatcher.group(1), context);
            } else if (commentMatcher.matches()) {
                // [impl->dsn~oft-equivalent.comment~1]
                context.setState(SpecificationConverter.State.COMMENT);
                appendTextBlock(commentMatcher.group(1), context);
            } else if (rationaleMatcher.matches()) {
                // [impl->dsn~oft-equivalent.rationale~1]
                context.setState(SpecificationConverter.State.RATIONALE);
                appendTextBlock(rationaleMatcher.group(1), context);
            } else if (coversMatcher.matches()) {
                context.setState(SpecificationConverter.State.COVERS);
            } else if (dependsMatcher.matches()) {
                context.setState(SpecificationConverter.State.DEPENDS);
            } else if (statusMatcher.matches()) {
                // [impl->dsn~oft-equivalent.status~1]
                context.getSpecListBuilder().setStatus(ItemStatus.parseString(statusMatcher.group(1)));
            } else {
                appendTextBlock(convertedBlock, context);
            }
        }
        return null;
    }

    private void appendTextBlock(String convertedBlock, ConversionContext context) {
        if (convertedBlock.trim().isEmpty()) {
            return;
        }
        if (context.getState() == SpecificationConverter.State.SPEC || context.getState() == SpecificationConverter.State.DESCRIPTION) {
            // [impl->dsn~oft-equivalent.description~2]
            context.getSpecListBuilder().appendDescription(convertedBlock);
        } else if (context.getState() == SpecificationConverter.State.COMMENT) {
            context.getSpecListBuilder().appendComment(convertedBlock);
        } else if (context.getState() == SpecificationConverter.State.RATIONALE) {
            context.getSpecListBuilder().appendRationale(convertedBlock);
        }
    }

}
