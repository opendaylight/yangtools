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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithTypedefNamespace;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.OperationContainerMixin;

public final class DeclaredOutputEffectiveStatement extends WithTypedefNamespace<QName, @NonNull OutputStatement>
        implements OutputEffectiveStatement, OutputSchemaNode, OperationContainerMixin<@NonNull OutputStatement> {
    private final @NonNull QName argument;
    private final int flags;

    public DeclaredOutputEffectiveStatement(final @NonNull OutputStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final QName argument,
            final int flags) {
        super(declared, substatements);
        this.argument = requireNonNull(argument);
        this.flags = flags;
    }

    public DeclaredOutputEffectiveStatement(final DeclaredOutputEffectiveStatement original, final QName argument,
            final int flags) {
        super(original);
        this.argument = requireNonNull(argument);
        this.flags = flags;
    }

    @Override
    public QName argument() {
        return argument;
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
}
