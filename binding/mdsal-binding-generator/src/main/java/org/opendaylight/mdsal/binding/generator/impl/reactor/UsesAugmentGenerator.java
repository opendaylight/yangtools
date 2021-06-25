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

import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;

/**
 * Generator corresponding to a {@code augment} statement used as a child of a {@code uses} statement.
 */
final class UsesAugmentGenerator extends AbstractAugmentGenerator {
    private final UsesEffectiveStatement uses;

    private GroupingGenerator grouping;

    UsesAugmentGenerator(final AugmentEffectiveStatement statement, final AbstractCompositeGenerator<?> parent,
            final UsesEffectiveStatement uses) {
        super(statement, parent);
        this.uses = requireNonNull(uses);
    }

    void linkGroupingDependency(final UsesEffectiveStatement checkUses, final GroupingGenerator resolvedGrouping) {
        if (uses.equals(checkUses)) {
            verify(grouping == null, "Attempted to relink %s from %s to %s", this, grouping, resolvedGrouping);
            this.grouping = requireNonNull(resolvedGrouping);
        }
    }

    @Override
    void loadTargetGenerator() {
        final GroupingGenerator grp = verifyNotNull(grouping, "No grouping linked in %s", this);
        final SchemaNodeIdentifier path = statement().argument();

        /*
         *  Here we are going in the opposite direction of RFC7950, section 3.13:
         *
         *        The effect of a "uses" reference to a grouping is that the nodes
         *        defined by the grouping are copied into the current schema tree and
         *        are then updated according to the "refine" and "augment" statements.
         *
         *  Our argument is composed of QNames in the current schema tree's namespace, but the grouping may have been
         *  defined in a different module -- and therefore it knows those children under its namespace. Adjust the path
         *  we are searching if that happens to be the case.
         */
        setTargetGenerator(grp.resolveSchemaNode(path, grp.statement().argument().getModule()));
    }
}
