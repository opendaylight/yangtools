/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement.SchemaTreeNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;

/**
 * Generator corresponding to a {@code augment} statement used as a child of a {@code uses} statement.
 */
final class UsesAugmentGenerator extends AbstractAugmentGenerator {
    private final UsesEffectiveStatement uses;

    private GroupingGenerator grouping;

    UsesAugmentGenerator(final AugmentEffectiveStatement statement, final UsesEffectiveStatement uses,
            final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
        this.uses = requireNonNull(uses);

        // FIXME: use SchemaTreeAwareEffectiveStatement
        var stmt = parent.statement();
        for (var qname : statement.argument().getNodeIdentifiers()) {
            final var tmp = stmt;
            stmt = stmt.streamEffectiveSubstatements(SchemaTreeEffectiveStatement.class)
                .filter(child -> qname.equals(child.argument()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to find " + qname + " in " + tmp));
        }
        setTargetStatement(stmt);
    }

    void resolveGrouping(final UsesEffectiveStatement resolvedUses, final GroupingGenerator resolvedGrouping) {
        if (resolvedUses == uses) {
            verify(grouping == null, "Attempted to re-resolve grouping of %s", this);
            grouping = requireNonNull(resolvedGrouping);
        }
    }

    @NonNull AugmentRequirement startLinkage() {
        // Here we are going in the opposite direction of RFC7950, section 7.13:
        //
        //    The effect of a "uses" reference to a grouping is that the nodes
        //    defined by the grouping are copied into the current schema tree and
        //    are then updated according to the "refine" and "augment" statements.
        //
        // Our parent here is *not* the 'uses' statement, but rather the statement which contains it.
        return new AugmentRequirement(this, verifyNotNull(grouping, "Unresolved grouping in %s", this));
    }

    @Override
    List<CaseRuntimeType> augmentedCasesIn(final ChildLookup lookup, final ChoiceEffectiveStatement stmt) {
        final var result = super.augmentedCasesIn(lookup, stmt);
        if (result != null) {
            return result;
        }
        final var augment = statement();
        if (!lookup.contains(augment)) {
            return List.of();
        }

        final var effectiveStatement = effectiveStatement(augment, stmt);
        return createBuilder(effectiveStatement)
            .fillTypes(lookup.inStatement(effectiveStatement), this)
            .getCaseChilden();
    }

    @Override
    AugmentRuntimeType runtimeTypeIn(final ChildLookup lookup, final EffectiveStatement<?, ?> target) {
        final var result = super.runtimeTypeIn(lookup, target);
        if (result != null) {
            return result;
        }
        final var augment = statement();
        if (!lookup.contains(augment)) {
            return null;
        }

        verify(target instanceof SchemaTreeAwareEffectiveStatement && target instanceof SchemaTreeEffectiveStatement,
            "Unexpected statement %s", target);
        final var effectiveStatement = effectiveStatement(augment, (SchemaTreeAwareEffectiveStatement<?, ?>) target);
        return verifyNotNull(createInternalRuntimeType(lookup.inStatement(effectiveStatement), effectiveStatement));
    }

    private static @NonNull AugmentEffectiveStatement effectiveStatement(final AugmentEffectiveStatement augment,
            final SchemaTreeAwareEffectiveStatement<?, ?> target) {
        verify(target instanceof SchemaTreeEffectiveStatement, "Unexpected statement %s", target);
        // 'uses'/'augment': our children are binding to target's namespace
        final var targetNamespace = ((SchemaTreeEffectiveStatement<?>) target).argument().getModule();

        final var stmts = augment.effectiveSubstatements();
        final var builder = ImmutableList.<EffectiveStatement<?, ?>>builderWithExpectedSize(stmts.size());
        for (var stmt : stmts) {
            if (stmt instanceof SchemaTreeEffectiveStatement) {
                final var qname = ((SchemaTreeEffectiveStatement<?>) stmt).getIdentifier().bindTo(targetNamespace);
                target.get(SchemaTreeNamespace.class, qname).ifPresent(builder::add);
            } else {
                builder.add(stmt);
            }
        }

        return new TargetAugmentEffectiveStatement(augment, target, builder.build());
    }
}
