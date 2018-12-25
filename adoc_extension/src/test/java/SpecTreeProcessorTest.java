import com.github.avonengel.adocspec.SpecTreeProcessor;
import org.asciidoctor.Asciidoctor;

import static org.asciidoctor.OptionsBuilder.options;

import org.hamcrest.Matchers;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

public class SpecTreeProcessorTest {

    @Test
    public void test() {
        // Arrange
        String input = "[.spec]\n" +
                "## A specification section";
        Asciidoctor asciidoctor = create();
        asciidoctor.unregisterAllExtensions();
        SpecTreeProcessor underTest = new SpecTreeProcessor();
        asciidoctor.javaExtensionRegistry().treeprocessor(underTest);

        // Act
        String output = asciidoctor.convert("", options().toFile(false));

        // Assert
        List<SpecificationItem> specObjects = underTest.getSpecObjects();
        assertThat(specObjects, notNullValue());
        assertThat(specObjects, not(empty()));
    }
}
