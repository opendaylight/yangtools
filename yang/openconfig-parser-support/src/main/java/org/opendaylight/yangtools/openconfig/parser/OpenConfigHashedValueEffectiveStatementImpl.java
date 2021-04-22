/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueStatement;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNodeDefaults;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SchemaPathSupport;

final class OpenConfigHashedValueEffectiveStatementImpl
        extends UnknownEffectiveStatementBase<Empty, OpenConfigHashedValueStatement>
        implements OpenConfigHashedValueEffectiveStatement {
    private final @NonNull StatementDefinition definition;
    private final @NonNull Immutable path;

    OpenConfigHashedValueEffectiveStatementImpl(final Current<Empty, OpenConfigHashedValueStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt, substatements);
        definition = stmt.publicDefinition();
        path = SchemaPathSupport.toEffectivePath(stmt.getEffectiveParent().getSchemaPath()
                .createChild(stmt.publicDefinition().getStatementName()));
    }

    @Override
    public QName getQName() {
        return SchemaNodeDefaults.extractQName(path);
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return SchemaNodeDefaults.extractPath(this, path);
    }

    @Override
    public StatementDefinition statementDefinition() {
        return definition;
    }

    @Override
    public OpenConfigHashedValueEffectiveStatement asEffectiveStatement() {
        return this;
    }
}