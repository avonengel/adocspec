package com.github.avonengel.adocspec;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.List;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.itsallcode.openfasttrace.core.SpecificationItemId;

import java.util.function.Consumer;
import java.util.regex.Matcher;

public class ListPipeFilter implements NodePipeFilter {
    @Override
    public Object handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof List) {
            List list = (List) node;
            if (context.getState() == SpecificationConverter.State.COVERS) {
                // [impl->dsn~oft-equivalent.covers~1]
                readSpecificationItemIdList(list, context.getSpecListBuilder()::addCoveredId);
                context.setState(SpecificationConverter.State.SPEC);
            } else if (context.getState() == SpecificationConverter.State.DEPENDS) {
                // [impl->dsn~oft-equivalent.depends-list~1]
                readSpecificationItemIdList(list, context.getSpecListBuilder()::addDependsOnId);
                context.setState(SpecificationConverter.State.SPEC);
            }
        }
        return null;
    }

    private void readSpecificationItemIdList(List list, Consumer<SpecificationItemId> idConsumer) {
        for (StructuralNode item : list.getItems()) {
            if (item instanceof ListItem) {
                ListItem listItem = (ListItem) item;
                String itemContent = listItem.getText();
                final Matcher idMatcher = SpecificationItemId.ID_PATTERN.matcher(itemContent);
                if (idMatcher.matches()) {
                    idConsumer.accept(SpecificationItemId.parseId(itemContent));
                }
            }
        }
    }
}
