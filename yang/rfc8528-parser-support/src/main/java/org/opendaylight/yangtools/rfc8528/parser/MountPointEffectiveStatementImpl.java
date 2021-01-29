/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.parser;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointEffectiveStatement;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchemaNode;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNodeDefaults;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

final class MountPointEffectiveStatementImpl extends UnknownEffectiveStatementBase<QName, MountPointStatement>
        implements MountPointEffectiveStatement, MountPointSchemaNode {
    private final @Nullable SchemaPath path;

    MountPointEffectiveStatementImpl(final Current<QName, MountPointStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final @Nullable SchemaPath path) {
        super(stmt, substatements);
        this.path = path;
    }

    @Override
    public QName getQName() {
        return verifyNotNull(argument());
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return SchemaNodeDefaults.throwUnsupportedIfNull(this, path);
    }

    @Override
    public MountPointEffectiveStatement asEffectiveStatement() {
        return this;
    }
}