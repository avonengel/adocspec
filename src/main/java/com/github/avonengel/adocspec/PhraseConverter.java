package com.github.avonengel.adocspec;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.PhraseNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PhraseConverter implements NodeHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PhraseConverter.class);

    @Override
    public Optional<Object> handleNode(ContentNode node, ConversionContext context) {
        if (node instanceof PhraseNode) {
            PhraseNode phrase = (PhraseNode) node;
            LOG.info("phrase target {}", phrase.getTarget());
            LOG.info("phrase text {}", phrase.getText());
            LOG.info("phrase context {}", phrase.getContext());
            LOG.info("phrase reftext {}", phrase.getReftext());
            LOG.info("phrase type {}", phrase.getType());
            LOG.info("phrase id {}", phrase.getId());
            LOG.info("phrase nodename {}", phrase.getNodeName());
            LOG.info("phrase role {}", phrase.getRoles());

            // inline anchors are represented as Phrase - they have null text attributes
            return Optional.ofNullable(phrase.getText());
        }
        return null;
    }
}
