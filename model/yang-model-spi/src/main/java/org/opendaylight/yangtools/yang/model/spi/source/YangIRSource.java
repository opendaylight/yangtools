/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceException;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;

/**
 * A {@link YangSourceRepresentation} backed by an {@link IRStatement}.
 */
@NonNullByDefault
public abstract sealed class YangIRSource implements YangSourceRepresentation, SourceInfo.Extractor
        permits YangIRModuleSource, YangIRSubmoduleSource {
    private final SourceIdentifier sourceId;
    private final IRStatement statement;
    private final @Nullable String symbolicName;

    YangIRSource(final SourceIdentifier sourceId, final IRStatement statement, final @Nullable String symbolicName) {
        this.sourceId = requireNonNull(sourceId);
        this.statement = statement;
        this.symbolicName = symbolicName;
    }

    /**
     * Create a new {@link YangIRSource} encapsulating a root {@link IRStatement} with specified
     * {@link SourceIdentifier} and an optional symbolic name.
     *
     * @param sourceId the {@link SourceIdentifier}
     * @param statement the root {@link IRStatement}
     * @param symbolicName optional symbolic name
     * @return A {@link YangIRSource}
     * @throws StatementSourceException if the {@code statement} is not a valid root
     */
    public static YangIRSource of(final SourceIdentifier sourceId, final IRStatement statement,
            final @Nullable String symbolicName) {
        final var keyword = statement.keyword();
        if (!(keyword instanceof IRKeyword.Unqualified unqualified)) {
            throw new StatementSourceException(refOf(sourceId, statement),
                "Root statement has invalid keyword " + keyword);
        }
        if (statement.argument() == null) {
            throw new StatementSourceException(refOf(sourceId, statement), "Root statement does not have an argument");
        }

        final var rootName = unqualified.identifier();
        return switch (rootName) {
            case "module" -> new YangIRModuleSource(sourceId, statement, symbolicName);
            case "submodule" -> new YangIRSubmoduleSource(sourceId, statement, symbolicName);
            default -> throw new StatementSourceException(refOf(sourceId, statement),
                "Invalid root statement keyword " + rootName);
        };
    }

    /**
     * {@return the root statement of this source}
     */
    public final IRStatement statement() {
        return statement;
    }

    @Override
    public final SourceIdentifier sourceId() {
        return sourceId;
    }

    @Override
    public final @Nullable String symbolicName() {
        return symbolicName;
    }

    @Override
    public final Class<YangIRSource> getType() {
        return YangIRSource.class;
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("identifier", sourceId).toString();
    }

    static final StatementDeclaration.InText refOf(final SourceIdentifier source, final IRStatement stmt) {
        return StatementDeclarations.inText(source.name().getLocalName(), stmt.startLine(), stmt.startColumn() + 1);
    }
}
