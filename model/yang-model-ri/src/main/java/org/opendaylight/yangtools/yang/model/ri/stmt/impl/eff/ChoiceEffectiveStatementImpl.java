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
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.AugmentationTargetMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MandatoryMixin;

public final class ChoiceEffectiveStatementImpl
        extends DefaultWithDataTree<QName, ChoiceStatement, ChoiceEffectiveStatement>
        implements ChoiceEffectiveStatement, ChoiceSchemaNode, DataSchemaNodeMixin<ChoiceStatement>,
            AugmentationTargetMixin<QName, ChoiceStatement>, MandatoryMixin<QName, ChoiceStatement> {
    private final CaseSchemaNode defaultCase;
    private final @NonNull QName argument;
    private final int flags;

    public ChoiceEffectiveStatementImpl(final ChoiceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final QName argument,
            final int flags, final @Nullable CaseSchemaNode defaultCase) {
        super(declared, substatements);
        this.argument = requireNonNull(argument);
        this.flags = flags;
        this.defaultCase = defaultCase;
    }

    public ChoiceEffectiveStatementImpl(final ChoiceEffectiveStatementImpl origEffective, final QName argument,
            final int flags) {
        super(origEffective);
        this.argument = requireNonNull(argument);
        this.flags = flags;
        defaultCase = origEffective.defaultCase;
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
    public Optional<? extends CaseSchemaNode> findCase(final QName qname) {
        final SchemaTreeEffectiveStatement<?> child = schemaTreeNamespace().get(requireNonNull(qname));
        return child instanceof CaseSchemaNode ? Optional.of((CaseSchemaNode) child) : Optional.empty();
    }

    @Override
    public Collection<? extends @NonNull CaseSchemaNode> getCases() {
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
        return ChoiceEffectiveStatementImpl.class.getSimpleName() + "[" + "qname=" + argument + "]";
    }
}
