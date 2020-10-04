/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.choice;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultWithSchemaTree.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.AugmentationTargetMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.MandatoryMixin;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class ChoiceEffectiveStatementImpl extends WithSubstatements<QName, ChoiceStatement, ChoiceEffectiveStatement>
        implements ChoiceEffectiveStatement, ChoiceSchemaNode, DerivableSchemaNode,
                   DataSchemaNodeMixin<QName, ChoiceStatement>, AugmentationTargetMixin<QName, ChoiceStatement>,
                   MandatoryMixin<QName, ChoiceStatement> {
    private final CaseSchemaNode defaultCase;
    private final ChoiceSchemaNode original;
    private final @NonNull SchemaPath path;
    private final int flags;

    ChoiceEffectiveStatementImpl(final ChoiceStatement declared,
            final StmtContext<QName, ChoiceStatement, ChoiceEffectiveStatement> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final int flags,
            final @Nullable CaseSchemaNode defaultCase, final @Nullable ChoiceSchemaNode original) {
        super(declared, ctx, substatements);
        this.flags = flags;
        this.path = ctx.getSchemaPath().get();
        this.defaultCase = defaultCase;
        this.original = original;
    }

    @Override
    public @NonNull QName argument() {
        return getQName();
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
    public Optional<ChoiceSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public Optional<? extends CaseSchemaNode> findCase(final QName qname) {
        final SchemaTreeEffectiveStatement<?> child = schemaTreeNamespace().get(requireNonNull(qname));
        return child instanceof CaseSchemaNode ? Optional.of((CaseSchemaNode) child) : Optional.empty();
    }

    @Override
    public Collection<? extends CaseSchemaNode> getCases() {
        return filterEffectiveStatements(CaseSchemaNode.class);
    }

    @Override
    public Optional<CaseSchemaNode> getDefaultCase() {
        return Optional.ofNullable(defaultCase);
    }

    @Override
    public ChoiceEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public String toString() {
        return ChoiceEffectiveStatementImpl.class.getSimpleName() + "[" + "qname=" + getQName() + "]";
    }
}
