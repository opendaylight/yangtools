/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.OperationDefinitionMixin;

public final class RpcEffectiveStatementImpl extends WithSubstatements<QName, RpcStatement, RpcEffectiveStatement>
        implements RpcDefinition, RpcEffectiveStatement, OperationDefinitionMixin<RpcStatement> {
    private final @NonNull Immutable path;
    private final int flags;

    public RpcEffectiveStatementImpl(final RpcStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final Immutable path, final int flags) {
        super(declared, substatements);
        this.path = requireNonNull(path);
        this.flags = flags;
    }

    @Override
    public Immutable pathObject() {
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
