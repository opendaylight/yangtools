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
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.CopyableMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.OperationDefinitionMixin;

public final class ActionEffectiveStatementImpl
        extends WithSubstatements<QName, ActionStatement, ActionEffectiveStatement>
        implements ActionDefinition, ActionEffectiveStatement, OperationDefinitionMixin<ActionStatement>,
                   CopyableMixin<QName, ActionStatement> {
    private final @NonNull Immutable path;
    private final int flags;

    public ActionEffectiveStatementImpl(final ActionStatement declared, final Immutable path, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
        this.path = requireNonNull(path);
        this.flags = flags;
    }

    public ActionEffectiveStatementImpl(final ActionEffectiveStatementImpl original, final Immutable path,
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
    public ActionEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
