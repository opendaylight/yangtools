/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.rpc;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.OperationDefinitionMixin;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class RpcEffectiveStatementImpl extends WithSubstatements<QName, RpcStatement, RpcEffectiveStatement>
        implements RpcDefinition, RpcEffectiveStatement, OperationDefinitionMixin<RpcStatement> {
    private final @NonNull SchemaPath path;
    private final int flags;

    RpcEffectiveStatementImpl(final RpcStatement declared, final SchemaPath path, final int flags,
            final StmtContext<QName, RpcStatement, RpcEffectiveStatement> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, ctx, substatements);
        this.path = requireNonNull(path);
        this.flags = flags;
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public RpcEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
