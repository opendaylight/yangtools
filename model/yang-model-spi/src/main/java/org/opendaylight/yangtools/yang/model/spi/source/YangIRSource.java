/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
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
public final class YangIRSource implements YangSourceRepresentation, SourceInfoAware {
    private final SourceIdentifier sourceId;
    private final IRStatement statement;
    private final @Nullable String symbolicName;

    /**
     * Default constructor.
     *
     * @param sourceId the {@link SourceIdentifier}
     * @param statement the root statement
     * @param symbolicName the symbolic name
     * @throws StatementSourceException if the source statement is invalid
     */
    public YangIRSource(final SourceIdentifier sourceId, final IRStatement statement,
            final @Nullable String symbolicName) {
        final var rootKeyword = statement.keyword();
        if (!(rootKeyword instanceof IRKeyword.Unqualified unqualified)) {
            throw new StatementSourceException(refOf(sourceId, statement),
                "Root statement has invalid keyword " + rootKeyword);
        }
        final var rootName = unqualified.identifier();
        switch (rootName) {
            case "module", "submodule" -> {
                // Okay
            }
            default -> throw new StatementSourceException(refOf(sourceId, statement),
                "Invalid root statement keyword " + rootName);
        }
        if (statement.argument() == null) {
            throw new StatementSourceException(refOf(sourceId, statement), "Root statement does not have an argument");
        }
        this.sourceId = requireNonNull(sourceId);
        this.statement = statement;
        this.symbolicName = symbolicName;
    }

    /**
     * Return the root statement of this source.
     *
     * @return Root statement.
     */
    public IRStatement statement() {
        return statement;
    }

    @Override
    public SourceIdentifier sourceId() {
        return sourceId;
    }

    @Override
    public @Nullable String symbolicName() {
        return symbolicName;
    }

    @Override
    public Class<YangIRSource> getType() {
        return YangIRSource.class;
    }

    @Override
    public SourceInfo sourceInfo() {
        final var keyword = statement.keyword().identifier();
        return switch (keyword) {
            case "module" -> moduleSourceInfo();
            case "submodule" -> submoduleSourceInfo();
            // This should never happen as we check in the constructor
            default -> throw new VerifyException("Unhandled root statement " + keyword);
        };
    }

    private SourceInfo.Module moduleSourceInfo() {

    }

    private SourceInfo.Submodule submoduleSourceInfo() {

    }

    // FIXME: hide this method
    @Beta
    public static StatementDeclaration.InText refOf(final SourceIdentifier source, final IRStatement stmt) {
        return StatementDeclarations.inText(source.name().getLocalName(), stmt.startLine(), stmt.startColumn() + 1);
    }
}
