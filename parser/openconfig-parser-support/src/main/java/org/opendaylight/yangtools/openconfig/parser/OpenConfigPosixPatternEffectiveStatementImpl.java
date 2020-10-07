/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigPosixPatternEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigPosixPatternStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SchemaPathSupport;

final class OpenConfigPosixPatternEffectiveStatementImpl
        extends UnknownEffectiveStatementBase<String, OpenConfigPosixPatternStatement>
        implements OpenConfigPosixPatternEffectiveStatement {
    private final SchemaPath path;

    OpenConfigPosixPatternEffectiveStatementImpl(final Current<String, OpenConfigPosixPatternStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt, substatements);
        path = SchemaPathSupport.toOptionalPath(stmt.getEffectiveParent().getSchemaPath()
            .createChild(OpenConfigStatements.OPENCONFIG_POSIX_PATTERN.getStatementName()));
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