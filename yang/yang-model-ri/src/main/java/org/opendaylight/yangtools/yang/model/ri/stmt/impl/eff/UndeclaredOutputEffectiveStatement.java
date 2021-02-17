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
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractUndeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.OperationContainerMixin;

public final class UndeclaredOutputEffectiveStatement
        extends WithSubstatements<QName, OutputStatement, OutputEffectiveStatement>
        implements OutputEffectiveStatement, OutputSchemaNode, OperationContainerMixin<OutputStatement> {
    private final @NonNull Immutable path;
    private final int flags;

    public UndeclaredOutputEffectiveStatement(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final Immutable path, final int flags) {
        super(substatements);
        this.path = requireNonNull(path);
        this.flags = flags;
    }

    public UndeclaredOutputEffectiveStatement(final UndeclaredOutputEffectiveStatement original, final Immutable path,
            final int flags) {
        super(original);
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
    public DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public OutputEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public String toString() {
        return defaultToString();
    }
}
