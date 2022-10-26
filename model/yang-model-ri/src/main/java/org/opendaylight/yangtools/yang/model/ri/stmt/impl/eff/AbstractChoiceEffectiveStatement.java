/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithDataTree;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.AugmentationTargetMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MandatoryMixin;

public abstract class AbstractChoiceEffectiveStatement extends DefaultWithDataTree<QName, ChoiceStatement>
        implements ChoiceEffectiveStatement, ChoiceSchemaNode, DataSchemaNodeMixin<ChoiceStatement>,
            AugmentationTargetMixin<QName, ChoiceStatement>, MandatoryMixin<QName, ChoiceStatement> {
    private final int flags;

    AbstractChoiceEffectiveStatement(final ChoiceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final int flags) {
        super(declared, substatements);
        this.flags = flags;
    }

    AbstractChoiceEffectiveStatement(final AbstractChoiceEffectiveStatement original, final int flags) {
        super(original);
        this.flags = flags;
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
    public final Optional<? extends CaseSchemaNode> findCaseNode(final QName qname) {
        final var child = findSchemaTreeNode(qname).orElse(null);
        return child instanceof CaseSchemaNode childCase ? Optional.of(childCase) : Optional.empty();
    }

    @Override
    public final Collection<? extends @NonNull CaseSchemaNode> getCases() {
        return filterEffectiveStatements(CaseSchemaNode.class);
    }

    @Override
    public final ChoiceEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
