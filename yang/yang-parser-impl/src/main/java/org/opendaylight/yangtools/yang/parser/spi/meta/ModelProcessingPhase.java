package org.opendaylight.yangtools.yang.parser.spi.meta;

import javax.annotation.Nullable;

public enum ModelProcessingPhase {

    /**
     *
     * Cross-source relationship resolution phase.
     * <p>
     * In this phase of processing only statements which affects
     * cross-source relationship (e.g. imports / includes)
     * are processed.
     * <p>
     * At end of this phase all source related contexts should
     * be bind to their imports and includes to allow
     * visibility of custom defined statements in following
     * phases.
     */
    Linkage(null),
    StatementDefinition(Linkage),
    FullDeclaration(StatementDefinition),
    SemanticInference(FullDeclaration);


    private ModelProcessingPhase previousPhase;

    private ModelProcessingPhase(@Nullable ModelProcessingPhase previous) {
        this.previousPhase = previous;
    }

    public ModelProcessingPhase getPreviousPhase() {
        return previousPhase;
    }

}
