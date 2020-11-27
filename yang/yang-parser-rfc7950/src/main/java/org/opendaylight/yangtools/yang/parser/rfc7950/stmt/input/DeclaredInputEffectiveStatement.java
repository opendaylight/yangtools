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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.OperationContainerMixin;

final class DeclaredInputEffectiveStatement extends WithSubstatements<QName, InputStatement, InputEffectiveStatement>
        implements InputEffectiveStatement, InputSchemaNode, OperationContainerMixin<InputStatement> {
    private final @NonNull SchemaPath path;
    private final int flags;

    DeclaredInputEffectiveStatement(final int flags, final InputStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final SchemaPath path) {
        super(declared, substatements);
        this.flags = flags;
        this.path = requireNonNull(path);
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
    public Optional<DataSchemaNode> findDataChildByName(final QName name) {
        return findDataSchemaNode(name);
    }

    @Override
    public InputEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public String toString() {
        return defaultToString();
    }
}
