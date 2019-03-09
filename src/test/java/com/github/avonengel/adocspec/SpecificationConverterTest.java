package com.github.avonengel.adocspec;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.itsallcode.openfasttrace.ImportSettings;
import org.itsallcode.openfasttrace.Oft;
import org.itsallcode.openfasttrace.core.ItemStatus;
import org.itsallcode.openfasttrace.core.Location;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.SpecificationItemId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.assertj.core.api.Assertions.assertThat;

class SpecificationConverterTest {
    private static final String A_TYPE = "dummy";
    private static final String AN_OTHER_TYPE = "othertype";
    private static final String A_SPEC_NAME = "a-spec";
    private static final String AN_OTHER_SPEC_NAME = "an-other-spec";
    private static final int A_SPEC_VERSION = 3;
    private static final SpecificationItemId A_SPEC_ID = SpecificationItemId.createId(A_TYPE, A_SPEC_NAME, A_SPEC_VERSION);
    private static final SpecificationItemId AN_OTHER_SPEC_ID = SpecificationItemId.createId(AN_OTHER_TYPE, AN_OTHER_SPEC_NAME, A_SPEC_VERSION);
    private static final String A_DESCRIPTION = "some text content, to be used as description";
    private static final String A_PARAGRAPH = "some text content, not to be used by spec";
    private static final String A_RATIONALE = "some text content, to be used as rationale";
    private static final String A_COMMENT = "some text content, to be used as comment";
    private static final String A_TITLE = "some title";
    private static final String STATUS_DRAFT = "draft";
    private static Asciidoctor asciidoctor;

    @BeforeAll
    static void prepareAsciidoctor() {
        asciidoctor = create();
    }

    @BeforeEach
    void prepareSpecProcessor() {
        asciidoctor.unregisterAllExtensions();
        asciidoctor.javaConverterRegistry().register(SpecificationConverter.class, "spec");
        asciidoctor.javaExtensionRegistry().preprocessor(SourcemapPreprocessor.class);
    }

    // [test->dsn~oft-equivalent.id~1]
    @Test
    @DisplayName("When a Spec ID is found, then a Spec item is created")
    void whenIdIsFoundSpecIsCreated() {
        // Arrange
        String input = "`+" + A_SPEC_ID + "+`";

        // Act
        final List<SpecificationItem> output = convertToSpecList(input);

        // Assert
        assertThat(output).isNotEmpty();
        assertThat(output).extracting(SpecificationItem::getId).containsOnly(A_SPEC_ID);
    }

    // [test->dsn~oft-equivalent.source-file-line~1]
    @Test
    @DisplayName("When a Spec is created, then source file and line are set")
    void whenSpecIsCreatedThenSourceFileLine() {
        // Arrange
        String input = "\n\n\n" +
                "`+" + A_SPEC_ID + "+`";

        // Act
        final List<SpecificationItem> output = convertToSpecList(input);

        // Assert
        assertThat(output).isNotEmpty();
        assertThat(output).extracting(SpecificationItem::getId).containsOnly(A_SPEC_ID);
        assertThat(output).first().extracting(SpecificationItem::getLocation)
                .isEqualTo(Location.builder().path("<stdin>").line(4).build());
    }

    @Test
    @DisplayName("When a Spec is created, then source file and line are set")
    @SuppressWarnings("unchecked")
    void whenSpecIsCreatedThenSourceFileLine(@TempDir Path sourceDirectory) throws IOException {
        // Arrange
        final Path sourceFile = sourceDirectory.resolve("source.adoc");
        String input = "`+" + A_SPEC_ID + "+`";
        Files.write(sourceFile, input.getBytes());

        // Act
        asciidoctor.convertFile(sourceFile.toFile(), OptionsBuilder.options().backend("spec"), List.class);
        // when converting to file, AsciiDoctorJ returns null
        Path outputFile = sourceDirectory.resolve("source.xml");
        final ImportSettings settings = ImportSettings.builder().addInputs(sourceDirectory).build();
        final List<SpecificationItem> result = Oft.create().importItems(settings);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(SpecificationItem::getId).containsOnly(A_SPEC_ID);
        assertThat(result).first().extracting(SpecificationItem::getLocation)
                .isEqualTo(Location.builder().path(sourceFile.toAbsolutePath().toString()).line(1).build());
    }

    // [test->dsn~oft-equivalent.id~1]
    @Test
    @DisplayName("When a Spec ID is found inside a section, then a Spec item is created")
    void whenIdIsFoundInSectionSpecIsCreated() {
        // Arrange
        String input = "== A Section Title\n" +
                "`+" + A_SPEC_ID + "+`";

        // Act
        final List<SpecificationItem> output = convertToSpecList(input);

        // Assert
        assertThat(output).isNotEmpty();
        assertThat(output).extracting(SpecificationItem::getId).containsOnly(A_SPEC_ID);
    }

    @Test
    @DisplayName("When a new spec ID is found, then a new spec item is created")
    void whenNewIdThenSpecIsTerminated() {
        // Arrange
        String input = "== A Section Title\n" +
                "`+" + A_SPEC_ID + "+`\n\n" +
                A_PARAGRAPH + "\n\n" +
                "`+" + AN_OTHER_SPEC_ID + "+`";

        // Act
        final List<SpecificationItem> output = convertToSpecList(input);

        // Assert
        assertThat(output).hasSize(2);
        assertThat(output).first().extracting(SpecificationItem::getDescription).asString().isEqualTo(A_PARAGRAPH);
        assertThat(output).last().extracting(SpecificationItem::getDescription).asString().isEmpty();
    }

    @Test
    @DisplayName("When a new section begins, then the spec item is terminated")
    void whenSectionBeginsSpecIsTerminated() {
        // Arrange
        String input = "== A Section Title\n" +
                "`+" + A_SPEC_ID + "+`\n\n" +
                "== Another Section Title\n" +
                A_PARAGRAPH;

        // Act
        final List<SpecificationItem> output = convertToSpecList(input);

        // Assert
        assertThat(output).isNotEmpty();
        assertThat(output).extracting(SpecificationItem::getDescription).first().asString()
                .doesNotContain(A_PARAGRAPH);
    }

    @Test
    void onlyParagraphsAfterSectionIdAreUsed() {
        // Arrange
        String input = A_PARAGRAPH + "\n\n" +
                "`+" + A_SPEC_ID + "+`";

        // Act
        final List<SpecificationItem> output = convertToSpecList(input);

        // Assert
        assertThat(output).isNotEmpty();
        assertThat(output).extracting(SpecificationItem::getDescription).first().asString()
                .isEmpty();
    }

    // [test->dsn~oft-equivalent.forwarding_needed_coverage~1]
    @Test
    @DisplayName("Coverage may be forwarded")
    void forwardedCoverage() {
        // Arrange
        String forwardingType = "forwardingtype";
        String neededType = "neededtype";
        String input = forwardingType + " -> " + neededType + " : " + "`+" + A_SPEC_ID + "+`";
        SpecificationItem expectedSpec = SpecificationItem.builder()
                .addCoveredId(A_SPEC_ID)
                .forwards(true)
                .addNeedsArtifactType(neededType)
                .id(forwardingType, A_SPEC_ID.getName(), A_SPEC_ID.getRevision())
                .build();

        // Act
        final List<SpecificationItem> output = convertToSpecList(input);

        // Assert
        assertThat(output).first().isEqualToComparingFieldByField(expectedSpec);
    }

    @Test
    @DisplayName("when forwarding item is found, then previous item is completed")
    void whenForwardingItemThenPreviousItemIsCompleted() {
        // Arrange
        String forwardingType = "forwardingtype";
        String neededType = "neededtype";
        String input = "`+" + A_SPEC_ID + "+`\n\n" +
                forwardingType + " -> " + neededType + " : " + "`+" + A_SPEC_ID + "+`";
        SpecificationItemId expectedId = SpecificationItemId.createId(forwardingType, A_SPEC_ID.getName(), A_SPEC_ID.getRevision());
        // Act
        final List<SpecificationItem> output = convertToSpecList(input);

        // Assert
        assertThat(output).hasSize(2);
        assertThat(output).extracting(SpecificationItem::getId).containsExactly(A_SPEC_ID, expectedId);
    }

    @SuppressWarnings("unchecked")
    private List<SpecificationItem> convertToSpecList(String input) {
        return asciidoctor.convert(input, OptionsBuilder.options().backend("spec"), List.class);
    }

    // [test->dsn~oft-equivalent.specification-item-title~1]
    @Test
    @DisplayName("When a title precedes a specification item's ID, the title is the specification item's title")
    void whenHeadingPrecedesIdThenShortDescription() {
        // Arrange
        String input = "## " + A_TITLE + "\n" +
                "`+" + A_SPEC_ID + "+`";

        // Act
        final List<SpecificationItem> output = convertToSpecList(input);

        // Assert
        assertThat(output).isNotEmpty();
        assertThat(output).extracting(SpecificationItem::getTitle).first().asString()
                .isEqualTo(A_TITLE);
    }

    @Test
    @DisplayName("When an ID is inside [example] block, it is not recognized as new item")
    void whenIdExampleThenNoNewItem() {
        // Arrange
        String input = "`+" + A_SPEC_ID + "+`\n\n" +
                "[example]" + "\n" +
                "====\n" +
                "+" + AN_OTHER_SPEC_ID + "+\n" +
                "====";

        // Act
        final List<SpecificationItem> output = convertToSpecList(input);

        // Assert
        assertThat(output).isNotEmpty();
        assertThat(output).extracting(SpecificationItem::getId)
                .containsOnly(A_SPEC_ID);
    }

    @DisplayName("Given a specification item ID")
    @Nested
    class GivenID {

        // [test->dsn~oft-equivalent.description~2]
        @Test
        @DisplayName("When a paragraph without any markers is found, then the paragraph becomes the spec's description")
        void whenParagraphThenDescription() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    A_DESCRIPTION;

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getDescription).containsOnly(A_DESCRIPTION);
        }

        @Test
        @DisplayName("When a paragraph starts with 'Description:', then the spec's description is set")
        void whenParagraphStartsWithDescriptionThenDescriptionIsSet() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    // First have another element than description, in order to distinguish from the default paragraph
                    "Comment: " + A_COMMENT + "\n\n" +
                    "Description: " + A_DESCRIPTION;

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getDescription)
                    .containsOnly(A_DESCRIPTION);
        }

        @Test
        @DisplayName("When a paragraph follows a 'Description:', then the spec's description is extended")
        void whenParagraphFollowsDescriptionThenDescriptionIsAppended() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    // First have another element than description, in order to distinguish from the default paragraph
                    "Comment: " + A_COMMENT + "\n\n" +
                    "Description: " + A_DESCRIPTION + "\n\n" +
                    A_DESCRIPTION;

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getDescription)
                    .first().asString().containsSequence(A_DESCRIPTION, A_DESCRIPTION);
        }

        // [test->dsn~oft-equivalent.needs~1]
        @Test
        @DisplayName("When a paragraph that starts with 'Needs:', then the spec's needs are set")
        void whenParagraphStartsWithNeedsThenNeedsIsSet() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    "Needs: " + AN_OTHER_TYPE;

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getNeedsArtifactTypes)
                    .first().asList().containsOnly(AN_OTHER_TYPE);
        }

        // [test->dsn~oft-equivalent.tags~1]
        @Test
        @DisplayName("When a paragraph that starts with 'Tags:', then the spec's tags are set")
        void whenParagraphStartsWithTagsThenTagsIsSet() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    "Tags: " + AN_OTHER_TYPE;

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getTags)
                    .first().asList().containsOnly(AN_OTHER_TYPE);
        }

        // [test->dsn~oft-equivalent.comment~1]
        @Test
        @DisplayName("When a paragraph that starts with 'Comment:', then the spec's comment is set")
        void whenParagraphStartsWithCommentThenCommentIsSet() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    "Comment: " + A_COMMENT;

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getComment)
                    .containsOnly(A_COMMENT);
            assertThat(output).extracting(SpecificationItem::getDescription)
                    .first().asString().isEmpty();
        }

        @Test
        @DisplayName("When a paragraph follows a comment paragraph, then it is appended to the comment")
        void whenParagraphFollowsCommentThenCommentIsAppended() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    "Comment: " + A_COMMENT + "\n\n" +
                    A_COMMENT;

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getComment)
                    .first().asString().containsSequence(A_COMMENT, A_COMMENT);
            assertThat(output).extracting(SpecificationItem::getDescription)
                    .first().asString().isEmpty();
        }

        // [test->dsn~oft-equivalent.rationale~1]
        @Test
        @DisplayName("When a paragraph that starts with 'Rationale:', then the spec's rationale is set")
        void whenParagraphStartsWithRationaleThenRationaleIsSet() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    "Rationale: " + A_RATIONALE;

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getRationale)
                    .containsOnly(A_RATIONALE);
            assertThat(output).extracting(SpecificationItem::getDescription)
                    .first().asString().isEmpty();
        }

        @Test
        @DisplayName("When a 'Rationale:' contains multiple lines, then the spec's rationale is set")
        void whenRationaleMultilineThenRationaleIsSet() {
            // Arrange
            final String multilineRationale = A_RATIONALE + "\n" + A_RATIONALE;
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    "Rationale: " + multilineRationale;

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getRationale)
                    .containsOnly(multilineRationale);
            assertThat(output).extracting(SpecificationItem::getDescription)
                    .first().asString().isEmpty();
        }

        @Test
        @DisplayName("When a paragraph follows a rationale paragraph, then it is appended to the rationale")
        void whenParagraphFollowsRationaleThenRationaleIsAppended() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    "Rationale: " + A_RATIONALE + "\n\n" +
                    A_RATIONALE;

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getRationale)
                    .first().asString().containsSequence(A_RATIONALE, A_RATIONALE);
            assertThat(output).extracting(SpecificationItem::getDescription)
                    .first().asString().isEmpty();
        }

        // [test->dsn~oft-equivalent.covers~1]
        @Test
        @DisplayName("When 'Covers:' is found, then the following list is converted to coverage links")
        void whenCoversThenListIsConvertedToCoverageLinks() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    "Covers: " + "\n\n" +
                    "* `+" + AN_OTHER_SPEC_ID + "+`";

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getCoveredIds)
                    .first().asList().containsOnly(AN_OTHER_SPEC_ID);
        }

        // [test->dsn~oft-equivalent.depends-list~1]
        @Test
        @DisplayName("When 'Depends:' is found, then the following list is converted to depends on links")
        void whenDependsThenListIsConvertedToDependsOnLinks() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    "Depends: " + "\n\n" +
                    "* `+" + AN_OTHER_SPEC_ID + "+`";

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getDependOnIds)
                    .first().asList().containsOnly(AN_OTHER_SPEC_ID);
        }

        // [test->dsn~oft-equivalent.status~1]
        @Test
        @DisplayName("When 'Status:' is found, remainder is used as status")
        void whenStatusThenStatusIsSet() {
            // Arrange
            String input = "`+" + A_SPEC_ID + "+`\n\n" +
                    "Status: " + STATUS_DRAFT;

            // Act
            final List<SpecificationItem> output = convertToSpecList(input);

            // Assert
            assertThat(output).isNotEmpty();
            assertThat(output).extracting(SpecificationItem::getStatus)
                    .containsOnly(ItemStatus.DRAFT);
        }
    }
}