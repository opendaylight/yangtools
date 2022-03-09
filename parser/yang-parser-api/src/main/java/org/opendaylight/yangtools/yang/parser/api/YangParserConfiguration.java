/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A configuration of {@link YangParser} wiring for use with {@link YangParserFactory}.
 */
@NonNullByDefault
public final class YangParserConfiguration implements Immutable {
    /**
     * System-wide default configuration.
     */
    public static final YangParserConfiguration DEFAULT = builder().build();

    private final ImportResolutionMode importResolutionMode;
    private final boolean retainDeclarationReferences;
    private final boolean warnForUnkeyedLists;

    private YangParserConfiguration(final ImportResolutionMode importResolutionMode,
            final boolean retainDeclarationReferences,
            final boolean warnForUnkeyedLists) {
        this.importResolutionMode = requireNonNull(importResolutionMode);
        this.retainDeclarationReferences = retainDeclarationReferences;
        this.warnForUnkeyedLists = warnForUnkeyedLists;
    }

    @Beta
    public ImportResolutionMode importResolutionMode() {
        return importResolutionMode;
    }

    public boolean retainDeclarationReferences() {
        return retainDeclarationReferences;
    }

    public boolean warnForUnkeyedLists() {
        return warnForUnkeyedLists;
    }

    @Override
    public int hashCode() {
        return Objects.hash(importResolutionMode, retainDeclarationReferences);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof YangParserConfiguration)) {
            return false;
        }
        final YangParserConfiguration other = (YangParserConfiguration) obj;
        return importResolutionMode == other.importResolutionMode
            && retainDeclarationReferences == other.retainDeclarationReferences;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("importResolution", importResolutionMode)
            .add("declarationReferences", retainDeclarationReferences)
            .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements Mutable {
        private ImportResolutionMode importResolutionMode = ImportResolutionMode.DEFAULT;
        private boolean retainDeclarationReferences;
        private boolean warnForUnkeyedLists;

        private Builder() {
            // Hidden on purpose
        }

        /**
         * Return a {@link YangParserConfiguration} initialized with contents of this builder.
         *
         * @return A YangParserConfiguration
         */
        public YangParserConfiguration build() {
            return new YangParserConfiguration(importResolutionMode, retainDeclarationReferences,
                    warnForUnkeyedLists);
        }

        @Beta
        public Builder importResolutionMode(final ImportResolutionMode newImportResolutionMode) {
            importResolutionMode = requireNonNull(newImportResolutionMode);
            return this;
        }

        public Builder retainDeclarationReferences(final boolean newRetainDeclarationReferences) {
            retainDeclarationReferences = newRetainDeclarationReferences;
            return this;
        }

        public Builder warnForUnkeyedLists(final boolean newWarnForUnkeyedLists) {
            warnForUnkeyedLists = newWarnForUnkeyedLists;
            return this;
        }
    }
}
