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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.ir.IRKeyword.Unqualified;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceException;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangSourceRepresentation;

public final class YangIRSchemaSource implements YangSourceRepresentation {
    private final @NonNull SourceIdentifier sourceId;
    private final @NonNull IRStatement rootStatement;
    private final @Nullable String symbolicName;

    public YangIRSchemaSource(final @NonNull SourceIdentifier sourceId, final @NonNull IRStatement rootStatement,
            final @Nullable String symbolicName) {
        final var rootKeyword = rootStatement.keyword();
        if (!(rootKeyword instanceof Unqualified)) {
            throw new StatementSourceException(refOf(sourceId, rootStatement),
                "Root statement has invalid keyword " + rootKeyword);
        }
        final var rootName = rootKeyword.identifier();
        switch (rootName) {
            case "module":
            case "submodule":
                break;
            default:
                throw new StatementSourceException(refOf(sourceId, rootStatement),
                    "Invalid root statement keyword " + rootName);
        }
        if (rootStatement.argument() == null) {
            throw new StatementSourceException(refOf(sourceId, rootStatement),
                "Root statement does not have an argument");
        }
        this.sourceId = requireNonNull(sourceId);
        this.rootStatement = rootStatement;
        this.symbolicName = symbolicName;
    }

    @Override
    public SourceIdentifier sourceId() {
        return sourceId;
    }

    @Override
    public String symbolicName() {
        return symbolicName;
    }

    @Override
    public Class<YangIRSchemaSource> getType() {
        return YangIRSchemaSource.class;
    }

    /**
     * Return the root statement of this source.
     *
     * @return Root statement.
     */
    public @NonNull IRStatement rootStatement() {
        return rootStatement;
    }

    // FIXME: hide this method
    @Beta
    public static StatementInText refOf(final SourceIdentifier source, final IRStatement stmt) {
        return StatementInText.atPosition(source.name().getLocalName(), stmt.startLine(),
            stmt.startColumn() + 1);
    }
}
