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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractUndeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;

public final class UndeclaredCaseEffectiveStatement
        extends WithSubstatements<QName, CaseStatement, CaseEffectiveStatement> implements CaseEffectiveStatementMixin {
    private final @Nullable CaseSchemaNode original;
    private final @NonNull Immutable path;
    private final int flags;

    public UndeclaredCaseEffectiveStatement(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final Immutable path, final int flags, final @Nullable CaseSchemaNode original) {
        super(substatements);
        this.path = requireNonNull(path);
        this.flags = flags;
        this.original = original;
    }

    public UndeclaredCaseEffectiveStatement(final UndeclaredCaseEffectiveStatement origEffective, final Immutable path,
            final int flags, final @Nullable CaseSchemaNode original) {
        super(origEffective);
        this.path = requireNonNull(path);
        this.flags = flags;
        this.original = original;
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public Immutable pathObject() {
        return path;
    }

    @Override
    @Deprecated(since = "7.0.9", forRemoval = true)
    public Optional<CaseSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public CaseEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public String toString() {
        return UndeclaredCaseEffectiveStatement.class.getSimpleName() + "[" + "qname=" + getQName() + "]";
    }
}
