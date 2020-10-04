/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.CopyableMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin.WithStatus;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.WhenConditionMixin;

/**
 * Empty {@link UsesEffectiveStatement}, at its very simplest form. This is appropriate when all of these are true:
 * <ul>
 *   <li>This statement takes effect in the same module as where it was declared, i.e. the arguments match</li>
 *   <li>It has no substatements</li>
 * </ul>
 *
 * <p>
 * This provides minimum footprint, as we share the argument from the declared instance and just keep the flags and
 * source grouping pointer.
 */
class EmptyLocalUsesEffectiveStatement extends DefaultArgument<QName, UsesStatement>
        implements UsesEffectiveStatement, UsesNode, CopyableMixin<QName, UsesStatement>,
            WhenConditionMixin<QName, UsesStatement>, WithStatus<QName, UsesStatement> {
    private final @NonNull GroupingDefinition sourceGrouping;
    private final int flags;

    EmptyLocalUsesEffectiveStatement(final UsesStatement declared, final GroupingDefinition sourceGrouping,
            final int flags) {
        super(declared);
        this.sourceGrouping = requireNonNull(sourceGrouping);
        this.flags = flags;
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
    public final UsesEffectiveStatement asEffectiveStatement() {
        return this;
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
