/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNodeDefaults;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractUndeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.OperationContainerMixin;

final class UndeclaredOutputEffectiveStatement
        extends WithSubstatements<QName, OutputStatement, OutputEffectiveStatement>
        implements OutputEffectiveStatement, OutputSchemaNode, OperationContainerMixin<OutputStatement> {
    private final @Nullable SchemaPath path;
    private final int flags;

    UndeclaredOutputEffectiveStatement(final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final SchemaPath path) {
        super(substatements);
        this.flags = flags;
        this.path = path;
    }

    UndeclaredOutputEffectiveStatement(final int flags, final UndeclaredOutputEffectiveStatement original,
            final SchemaPath path) {
        super(original);
        this.flags = flags;
        this.path = path;
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return SchemaNodeDefaults.throwUnsupportedIfNull(this, path);
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
