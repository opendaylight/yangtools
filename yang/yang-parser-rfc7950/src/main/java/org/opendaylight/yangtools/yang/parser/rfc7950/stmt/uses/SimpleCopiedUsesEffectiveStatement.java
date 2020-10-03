/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultWithArgument;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.CopyableMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin.WithStatus;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.WhenConditionMixin;

/**
 * A simple case of a copied statement. The key difference here is that the argument does not match the declared
 * argument -- i.e. the effective instance is in a different module. This also means that there is some history
 * copy history attached.
 *
 * <p>
 * Since we have to keep the argument already, we perform a different field cut than in the local case and handle
 * also substatements here. We do not handle further refines, though, as that requires yet another field, further
 * growing instance size. That case is handled by {@link FullCopiedUsesEffectiveStatement}.
 */
class SimpleCopiedUsesEffectiveStatement extends DefaultWithArgument.WithSubstatements<QName, UsesStatement>
        implements UsesEffectiveStatement, UsesNode, CopyableMixin<QName, UsesStatement>,
            WhenConditionMixin<QName, UsesStatement>, WithStatus<QName, UsesStatement> {
    private final @NonNull GroupingDefinition sourceGrouping;
    private final int flags;

    SimpleCopiedUsesEffectiveStatement(final UsesStatement declared, final QName argument,
            final GroupingDefinition sourceGrouping, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, argument, substatements);
        this.sourceGrouping = requireNonNull(sourceGrouping);
        this.flags = flags;
    }

    SimpleCopiedUsesEffectiveStatement(final UsesStatement declared, final QName argument,
            final GroupingDefinition sourceGrouping, final int flags) {
        this(declared, argument, sourceGrouping, flags, ImmutableList.of());
    }

    @Override
    public final GroupingDefinition getSourceGrouping() {
        return sourceGrouping;
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
    public final Collection<? extends AugmentationSchemaNode> getAugmentations() {
        return filterEffectiveStatements(AugmentationSchemaNode.class);
    }

    @Override
    public Map<Descendant, SchemaNode> getRefines() {
        return ImmutableMap.of();
    }
}
