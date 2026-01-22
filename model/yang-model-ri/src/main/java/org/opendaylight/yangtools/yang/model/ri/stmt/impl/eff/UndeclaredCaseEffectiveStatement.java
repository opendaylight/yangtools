/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractUndeclaredEffectiveStatement.DefaultWithDataTree;

public final class UndeclaredCaseEffectiveStatement extends DefaultWithDataTree<QName, @NonNull CaseStatement>
        implements CaseEffectiveStatementMixin {
    private final @NonNull QName argument;
    private final int flags;

    public UndeclaredCaseEffectiveStatement(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final QName argument, final int flags) {
        super(substatements);
        this.argument = requireNonNull(argument);
        this.flags = flags;
    }

    public UndeclaredCaseEffectiveStatement(final UndeclaredCaseEffectiveStatement origEffective, final QName argument,
            final int flags) {
        super(origEffective);
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
    public CaseEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
