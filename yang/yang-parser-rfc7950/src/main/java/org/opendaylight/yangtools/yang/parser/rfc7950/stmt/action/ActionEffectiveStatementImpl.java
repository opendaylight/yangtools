/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.action;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.CopyableMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.OperationDefinitionMixin;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class ActionEffectiveStatementImpl extends WithSubstatements<QName, ActionStatement, ActionEffectiveStatement>
        implements ActionDefinition, ActionEffectiveStatement, OperationDefinitionMixin<ActionStatement>,
                   CopyableMixin<QName, ActionStatement> {
    private final @NonNull SchemaPath path;
    private final int flags;

    ActionEffectiveStatementImpl(final ActionStatement declared, final SchemaPath path, final int flags,
            final StmtContext<QName, ActionStatement, ActionEffectiveStatement> ctx,
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
    public ActionEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
