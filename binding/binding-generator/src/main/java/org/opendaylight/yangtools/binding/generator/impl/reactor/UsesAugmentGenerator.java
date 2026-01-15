/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
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
    boolean matchesInstantiated(final AugmentEffectiveStatement statement) {
        return super.matchesInstantiated(statement) || declared(statement()).equals(declared(statement));
    }

    private static AugmentStatement declared(final AugmentEffectiveStatement statement) {
        // We are generating Augmentation interfaces on declared model, hence this is accurate
        return statement.requireDeclared();
    }

    @Override
    TargetAugmentEffectiveStatement effectiveIn(final SchemaTreeAwareEffectiveStatement<?, ?> target) {
        verify(target instanceof SchemaTreeEffectiveStatement, "Unexpected statement %s", target);
        // 'uses'/'augment': our children are binding to target's namespace

        final var targetNamespace = ((SchemaTreeEffectiveStatement<?>) target).argument().getModule();
        return effectiveIn(target, qname -> qname.bindTo(targetNamespace));
    }
}
