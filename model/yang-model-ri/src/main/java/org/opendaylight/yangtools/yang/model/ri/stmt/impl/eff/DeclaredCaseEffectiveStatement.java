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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree.WithSubstatements;

public final class DeclaredCaseEffectiveStatement
        extends WithSubstatements<QName, CaseStatement, CaseEffectiveStatement> implements CaseEffectiveStatementMixin {
    private final CaseSchemaNode original;
    private final @NonNull QName qname;
    private final int flags;

    public DeclaredCaseEffectiveStatement(final CaseStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final QName qname, final int flags,
            final @Nullable CaseSchemaNode original) {
        super(declared, substatements);
        this.qname = requireNonNull(qname);
        this.flags = flags;
        this.original = original;
    }

    public DeclaredCaseEffectiveStatement(final DeclaredCaseEffectiveStatement origEffective, final QName qname,
            final int flags, final @Nullable CaseSchemaNode original) {
        super(origEffective);
        this.qname = requireNonNull(qname);
        this.flags = flags;
        this.original = original;
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
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
        return DeclaredCaseEffectiveStatement.class.getSimpleName() + "[" + "qname=" + qname + "]";
    }
}
