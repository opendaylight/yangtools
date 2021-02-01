/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.input;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.OperationContainerMixin;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveSchemaTreeStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStatementStateAware;

final class DeclaredInputEffectiveStatement extends WithSubstatements<QName, InputStatement, InputEffectiveStatement>
        implements InputEffectiveStatement, InputSchemaNode, OperationContainerMixin<InputStatement>,
                   EffectiveStatementStateAware {
    private final @NonNull Immutable path;
    private final int flags;

    DeclaredInputEffectiveStatement(final InputStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final Immutable path,
            final int flags) {
        super(declared, substatements);
        this.path = requireNonNull(path);
        this.flags = flags;
    }

    DeclaredInputEffectiveStatement(final DeclaredInputEffectiveStatement original, final Immutable path,
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
    public InputEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public EffectiveStatementState toEffectiveStatementState() {
        return new EffectiveSchemaTreeStatementState(path, flags);
    }

    @Override
    public String toString() {
        return defaultToString();
    }
}
