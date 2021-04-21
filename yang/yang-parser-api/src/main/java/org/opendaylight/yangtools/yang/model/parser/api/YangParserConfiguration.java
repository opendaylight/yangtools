/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.parser.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;

/**
 * A configuration of {@link YangParser} wiring for use with {@link YangParserFactory}.
 */
@Beta
@NonNullByDefault
public final class YangParserConfiguration implements Immutable {
    /**
     * System-wide default configuration.
     */
    public static final YangParserConfiguration DEFAULT = builder().build();

    private final StatementParserMode parserMode;
    private final boolean retainDeclarationReferences;

    private YangParserConfiguration(final StatementParserMode parserMode, final boolean retainDeclarationReferences) {
        this.parserMode = requireNonNull(parserMode);
        this.retainDeclarationReferences = retainDeclarationReferences;
    }

    public StatementParserMode parserMode() {
        return parserMode;
    }

    public boolean retainDeclarationReferences() {
        return retainDeclarationReferences;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("parserMode", parserMode)
            .add("retainDeclarationReferences", retainDeclarationReferences)
            .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements org.opendaylight.yangtools.concepts.Builder<YangParserConfiguration> {
        private StatementParserMode parserMode = StatementParserMode.DEFAULT_MODE;
        private boolean retainDeclarationReferences = false;

        private Builder() {
            // Hidden on purpose
        }

        @Override
        public YangParserConfiguration build() {
            return new YangParserConfiguration(parserMode, retainDeclarationReferences);
        }

        public Builder parserMode(final StatementParserMode parserMode) {
            this.parserMode = requireNonNull(parserMode);
            return this;
        }

        public Builder retainDeclarationReferences(final boolean retainDeclarationReferences) {
            this.retainDeclarationReferences = retainDeclarationReferences;
            return this;
        }
    }
}
