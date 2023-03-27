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
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

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
            final boolean retainDeclarationReferences, final boolean warnForUnkeyedLists) {
        this.importResolutionMode = requireNonNull(importResolutionMode);
        this.retainDeclarationReferences = retainDeclarationReferences;
        this.warnForUnkeyedLists = warnForUnkeyedLists;
    }

    @Beta
    public ImportResolutionMode importResolutionMode() {
        return importResolutionMode;
    }

    /**
     * Return {@code true} if {@link DeclarationReference} to source location in the final parser product, notably
     * making {@link DeclaredStatement#declarationReference()} available.
     *
     * @return {@code true} if declaration references should be retained
     */
    public boolean retainDeclarationReferences() {
        return retainDeclarationReferences;
    }

    /**
     * Issue a warning when a {@code list} statement without a {@code key} statement is found in the
     * {@code config true} part of the schema tree. Such statements run contrary to
     * <a href="https://www.rfc-editor.org/rfc/rfc7950.html#section-7.8.2">RFC7950</a>, but are readily supported
     * by OpenDaylight infrastructure.
     *
     * @return {@code true} if non-compliant {@code list} statements should be reported
     */
    public boolean warnForUnkeyedLists() {
        return warnForUnkeyedLists;
    }

    @Override
    public int hashCode() {
        return Objects.hash(importResolutionMode, retainDeclarationReferences);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof YangParserConfiguration other
            && importResolutionMode == other.importResolutionMode
            && retainDeclarationReferences == other.retainDeclarationReferences;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("importResolution", importResolutionMode)
            .add("declarationReferences", retainDeclarationReferences)
            .toString();
    }

    /**
     * Return a new {@link Builder} initialized to default configuration.
     *
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements Mutable {
        private ImportResolutionMode importResolutionMode = ImportResolutionMode.DEFAULT;
        private boolean retainDeclarationReferences;
        // FIXME: YANGTOOLS-1423: default to false
        private boolean warnForUnkeyedLists = true;

        private Builder() {
            // Hidden on purpose
        }

        /**
         * Return a {@link YangParserConfiguration} initialized with contents of this builder.
         *
         * @return A YangParserConfiguration
         */
        public YangParserConfiguration build() {
            return new YangParserConfiguration(importResolutionMode, retainDeclarationReferences, warnForUnkeyedLists);
        }

        @Beta
        public Builder importResolutionMode(final ImportResolutionMode newImportResolutionMode) {
            importResolutionMode = requireNonNull(newImportResolutionMode);
            return this;
        }

        /**
         * Retain {@link DeclarationReference} to source location in the final parser product. This option results in
         * quite significant memory overhead for storage of {@link DeclaredStatement}, but makes
         * {@link DeclaredStatement#declarationReference()} available, which is useful in certain scenarios, for example
         * YANG editors.
         *
         * <p>
         * This option is disabled by default.
         *
         * @param newRetainDeclarationReferences {@code true} if declaration references should be retained
         * @return This builder
         */
        public Builder retainDeclarationReferences(final boolean newRetainDeclarationReferences) {
            retainDeclarationReferences = newRetainDeclarationReferences;
            return this;
        }

        /**
         * Issue a warning when a {@code list} statement without a {@code key} statement is found in the
         * {@code config true} part of the schema tree. Such statements run contrary to
         * <a href="https://www.rfc-editor.org/rfc/rfc7950.html#section-7.8.2">RFC7950</a>, but are readily supported
         * by OpenDaylight infrastructure.
         *
         * <p>
         * This option is enabled by default.
         *
         * @param newWarnForUnkeyedLists {@code true} if non-compliant {@code list} statements should be reported
         * @return This builder
         */
        public Builder warnForUnkeyedLists(final boolean newWarnForUnkeyedLists) {
            warnForUnkeyedLists = newWarnForUnkeyedLists;
            return this;
        }
    }
}
