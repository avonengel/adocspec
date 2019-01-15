package com.github.avonengel.adocspec;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.converter.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class SpecificationConverter extends StringConverter {

    private static final Logger LOG = LoggerFactory.getLogger(SpecificationConverter.class);

    public SpecificationConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
    }

    @Override
    public String convert(ContentNode node, String transform, Map<Object, Object> opts) {
        LOG.info("Asciidoctor converter: convert {} ({}), {}, {}", node, node.getNodeName(), transform, opts);
        if (node instanceof StructuralNode) {
            final StructuralNode structuralNode = (StructuralNode) node;
            structuralNode.getBlocks().forEach(this::convertBlock);
        } else if (node instanceof PhraseNode) {
            final PhraseNode phrase = (PhraseNode) node;
            LOG.info("phrase target {}", phrase.getTarget());
            LOG.info("phrase text {}", phrase.getText());
            LOG.info("phrase context {}", phrase.getContext());
            LOG.info("phrase reftext {}", phrase.getReftext());
            LOG.info("phrase type {}", phrase.getType());
            LOG.info("phrase id {}", phrase.getId());
            LOG.info("phrase nodename {}", phrase.getNodeName());
            LOG.info("phrase role {}", phrase.getRoles());

            // this might work, right?
            /*
             1. convert phrases to whatever the OFT people do with markup in markdown-formatted content
                this results in more-or-less plaintext that contains no markup artifacts (HTML5 or DocBook tags)
             2. convert blocks just like the SpecTreeProcessor
             3. return XMl-representation of the specification items
             4. idea for later: return java object instead of XML-string, only this write-to-stream thing on request
             */
            return "->" + phrase.getText() + "<-";
        }
        return "node type: " + node.getClass() + " node name: " + node.getNodeName() + "\n";
    }

    private void convertBlock(StructuralNode structuralNode) {
        LOG.info("converting type {} node name: {}", structuralNode.getClass(), structuralNode.getNodeName());
        LOG.debug("node ID: {}", structuralNode.getId());
        LOG.debug("node attributes: {}", structuralNode.getAttributes());
        LOG.debug("node title: {}", structuralNode.getTitle());
        LOG.debug("node context: {}", structuralNode.getContext());
//        LOG.debug("node content: {}", structuralNode.getContent());
        if(structuralNode instanceof Block) {
            final Block block = (Block) structuralNode;
            LOG.debug("block source: {}", block.getSource());
        }
        final List<StructuralNode> blocks = structuralNode.getBlocks();
        if (blocks != null && blocks.size() > 0) {
            LOG.info("Going down one more level ({} elements)", blocks.size());
            blocks.forEach(this::convertBlock);
        }
    }
}
