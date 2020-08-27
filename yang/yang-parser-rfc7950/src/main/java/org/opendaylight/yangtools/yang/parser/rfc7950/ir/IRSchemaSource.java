/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRKeyword.Unqualified;

@Beta
public final class IRSchemaSource extends AbstractIdentifiable<SourceIdentifier> implements SchemaSourceRepresentation {
    private final @NonNull IRStatement rootStatement;
    private final @Nullable String symbolicName;

    public IRSchemaSource(final @NonNull SourceIdentifier identifier, final @NonNull IRStatement rootStatement,
            @Nullable final String symbolicName) {
        super(identifier);
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

    public IRSchemaSource(final @NonNull SourceIdentifier identifier, final @NonNull IRStatement rootStatement) {
        this(identifier, rootStatement, null);
    }

    @Override
    public Optional<String> getSymbolicName() {
        return Optional.ofNullable(symbolicName);
    }

    @Override
    public Class<@NonNull IRSchemaSource> getType() {
        return IRSchemaSource.class;
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
