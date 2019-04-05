package com.github.avonengel.adocspec;

import java.util.Optional;

public class NodeResult<T> {
    private final Optional<T> result;
    private final boolean continueHandlers;

    public static <T> NodeResult<T> of(T result) {
        return new NodeResult<>(result);
    }

    public static <T> NodeResult<T> continueHandlers() {
        return new NodeResult<>(true);
    }

    public static <T> NodeResult<T> stopHandlers() {
        return new NodeResult<>(false);
    }

    private NodeResult(T result) {
        this.result = Optional.of(result);
        continueHandlers = false;
    }

    private NodeResult(boolean continueHandlers) {
        this.result = Optional.empty();
        this.continueHandlers = continueHandlers;
    }

    public Optional<T> getResult() {
        return result;
    }

    public boolean shouldContinue() {
        return continueHandlers;
    }
}
