/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRKeyword.Unqualified;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.spi.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.YangSchemaSourceRepresentation;

public final class YangIRSchemaSource implements YangSchemaSourceRepresentation {
    private final @NonNull SourceIdentifier sourceId;
    private final @NonNull IRStatement rootStatement;
    private final @Nullable String symbolicName;

    public YangIRSchemaSource(final @NonNull SourceIdentifier sourceId, final @NonNull IRStatement rootStatement,
            final @Nullable String symbolicName) {
        this.sourceId = requireNonNull(sourceId);
        this.rootStatement = requireNonNull(rootStatement);
        this.symbolicName = symbolicName;

        final IRKeyword rootKeyword = rootStatement.keyword();
        checkArgument(rootKeyword instanceof Unqualified, "Root statement has invalid keyword %s", rootKeyword);
        final String rootName = rootKeyword.identifier();
        switch (rootName) {
            case "module":
            case "submodule":
                break;
            default:
                throw new IllegalArgumentException("Invalid root statement keyword " + rootName);
        }

        checkArgument(rootStatement.argument() != null, "Root statement does not have an argument");
    }

    @Override
    public SourceIdentifier sourceId() {
        return sourceId;
    }

    @Override
    public Optional<String> getSymbolicName() {
        return Optional.ofNullable(symbolicName);
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
    public @NonNull IRStatement getRootStatement() {
        return rootStatement;
    }
}
