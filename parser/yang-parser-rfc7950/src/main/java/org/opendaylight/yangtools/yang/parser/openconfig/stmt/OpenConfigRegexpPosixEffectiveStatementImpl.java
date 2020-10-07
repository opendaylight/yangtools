/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.openconfig.stmt;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigRegexpPosixEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigRegexpPosixStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.SchemaPathSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

final class OpenConfigRegexpPosixEffectiveStatementImpl
        extends UnknownEffectiveStatementBase<Empty, OpenConfigRegexpPosixStatement>
        implements OpenConfigRegexpPosixEffectiveStatement {

    private final SchemaPath path;

    OpenConfigRegexpPosixEffectiveStatementImpl(final Current<Empty, OpenConfigRegexpPosixStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt, substatements);
        path = SchemaPathSupport.toOptionalPath(stmt.getEffectiveParent().getSchemaPath()
            .createChild(OpenConfigStatements.OPENCONFIG_REGEXP_POSIX.getStatementName()));
    }

    @Override
    public QName getQName() {
        return path.getLastComponent();
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public UnknownEffectiveStatement<?, ?> asEffectiveStatement() {
        return this;
    }
}