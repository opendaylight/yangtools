/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;

/**
 * Generator corresponding to a {@code augment} statement used as a child of a {@code uses} statement.
 */
final class UsesAugmentGenerator extends AbstractAugmentGenerator {
    UsesAugmentGenerator(final AugmentEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void loadTargetGenerator() {
        // Here we are going in the opposite direction of RFC7950, section 7.13:
        //
        //    The effect of a "uses" reference to a grouping is that the nodes
        //    defined by the grouping are copied into the current schema tree and
        //    are then updated according to the "refine" and "augment" statements.
        //
        // Our parent here is *not* the uses statement, but rather the statement which contains uses -- and its
        // getSchemaTreeGenerator() is well equipped to deal with the namespace hopping needed to perform the lookups
        setTargetGenerator(getParent().resolveSchemaNode(statement().argument()));
    }
}
