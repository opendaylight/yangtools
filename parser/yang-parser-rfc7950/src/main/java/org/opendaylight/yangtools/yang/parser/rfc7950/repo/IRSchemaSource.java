/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.AbstractSimpleIdentifiable;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRKeyword.Unqualified;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangSchemaSourceRepresentation;

@Beta
public final class IRSchemaSource extends AbstractSimpleIdentifiable<SourceIdentifier>
        implements YangSchemaSourceRepresentation {
    private final @NonNull IRStatement rootStatement;
    private final @Nullable String symbolicName;

    public IRSchemaSource(final @NonNull SourceIdentifier identifier, final @NonNull IRStatement rootStatement,
            final @Nullable String symbolicName) {
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

    @Override
    public Optional<String> getSymbolicName() {
        return Optional.ofNullable(symbolicName);
    }

    @Override
    public Class<IRSchemaSource> getType() {
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
