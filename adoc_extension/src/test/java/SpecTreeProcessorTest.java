import com.github.avonengel.adocspec.SpecTreeProcessor;
import org.asciidoctor.Asciidoctor;

import static java.util.stream.Collectors.toList;
import static org.asciidoctor.OptionsBuilder.options;

import org.hamcrest.Matchers;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("A SpecObject")
public class SpecTreeProcessorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SpecTreeProcessorTest.class);

    @Test
    @DisplayName("is defined by using the .spec role")
    public void whenSpecRoleIsFoundOnSectionSpecItemIsCreated() {
        // Arrange
        String input = "[.spec]\n" +
                "== A specification section";
        Asciidoctor asciidoctor = create();
        asciidoctor.unregisterAllExtensions();
        SpecTreeProcessor underTest = new SpecTreeProcessor();
        asciidoctor.javaExtensionRegistry().treeprocessor(underTest);

        // Act
        String output = asciidoctor.convert(input, options().toFile(false));
        LOG.info("Output: {}", output);

        // Assert
        List<SpecificationItem> specObjects = underTest.getSpecObjects();
        assertThat(specObjects, notNullValue());
        assertThat(specObjects, not(empty()));
    }

    @DisplayName("has a SpecificationItemId")
    @Nested
    class SpecificationItemIdTest {
        @Test
        @DisplayName("that can be defined in OFT syntax via specID attribute")
        void specItemIdCanBeDefinedInOftSyntax() {
            // Arrange
            String input = "[.spec,specID=feat~html-export~1]\n" +
                    "== HTML Export";
            Asciidoctor asciidoctor = create();
            asciidoctor.unregisterAllExtensions();
            SpecTreeProcessor underTest = new SpecTreeProcessor();
            asciidoctor.javaExtensionRegistry().treeprocessor(underTest);

            // Act
            String output = asciidoctor.convert(input, options().toFile(false));

            // Assert
            List<SpecificationItem> specObjects = underTest.getSpecObjects();
            assertThat(specObjects.stream()
                            .map(SpecificationItem::getId)
                            .collect(toList()),
                    hasItem(SpecificationItemId.parseId("feat~html-export~1")));
        }
    }
}
