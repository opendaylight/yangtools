/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithTypedefNamespace;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.CopyableMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.OperationDefinitionMixin;

public final class ActionEffectiveStatementImpl extends WithTypedefNamespace<QName, @NonNull ActionStatement>
        implements ActionDefinition, ActionEffectiveStatement, OperationDefinitionMixin<@NonNull ActionStatement>,
                   CopyableMixin<QName, @NonNull ActionStatement> {
    private final @NonNull QName argument;
    private final int flags;

    public ActionEffectiveStatementImpl(final @NonNull ActionStatement declared, final QName argument,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
        this.argument = requireNonNull(argument);
        this.flags = flags;
    }

    public ActionEffectiveStatementImpl(final ActionEffectiveStatementImpl original, final QName argument,
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
    public ActionEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
