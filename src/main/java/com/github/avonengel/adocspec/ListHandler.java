package com.github.avonengel.adocspec;

import org.asciidoctor.ast.List;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.itsallcode.openfasttrace.core.SpecificationItemId;

import java.util.function.Consumer;
import java.util.regex.Matcher;

abstract class ListHandler implements NodeHandler {

    void readSpecificationItemIdList(List list, Consumer<SpecificationItemId> idConsumer) {
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
