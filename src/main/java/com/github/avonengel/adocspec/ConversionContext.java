package com.github.avonengel.adocspec;

public class ConversionContext {
    private final BlockSpecListBuilder specListBuilder;
    private SpecificationConverter.State currentState = SpecificationConverter.State.START;

    public ConversionContext(BlockSpecListBuilder specListBuilder) {
        this.specListBuilder = specListBuilder;
    }

    public BlockSpecListBuilder getSpecListBuilder() {
        return specListBuilder;
    }

    public SpecificationConverter.State getState() {
        return currentState;
    }

    public void setState(SpecificationConverter.State state) {
        this.currentState = state;
    }

}
