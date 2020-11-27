/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.case_;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractUndeclaredEffectiveStatement.DefaultWithSchemaTree.WithSubstatements;

final class UndeclaredCaseEffectiveStatement extends WithSubstatements<QName, CaseStatement, CaseEffectiveStatement>
        implements CaseEffectiveStatementMixin {
    private final CaseSchemaNode original;
    private final @NonNull SchemaPath path;
    private final int flags;

    UndeclaredCaseEffectiveStatement(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final int flags, final SchemaPath path, final @Nullable CaseSchemaNode original) {
        super(substatements);
        this.path = requireNonNull(path);
        this.flags = flags;
        this.original = original;
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public Optional<CaseSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public Optional<DataSchemaNode> findDataChildByName(final QName name) {
        return findDataSchemaNode(name);
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
