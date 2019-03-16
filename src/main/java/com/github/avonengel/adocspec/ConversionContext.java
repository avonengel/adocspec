package com.github.avonengel.adocspec;

import java.util.Optional;

public class ConversionContext {
    private final BlockSpecListBuilder specListBuilder;
    private SpecificationConverter.State currentState = SpecificationConverter.State.START;
    private String lastTitle = null;

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

    public Optional<String> getLastTitle() {
        return Optional.ofNullable(lastTitle);
    }

    public void setLastTitle(String lastTitle) {
        this.lastTitle = lastTitle;
    }

    public void removeLastTitle() {
        this.lastTitle = null;
    }
}
