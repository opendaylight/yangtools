/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;

/**
 * Generator corresponding to a {@code augment} statement used as a child of a {@code module} statement.
 */
final class ModuleAugmentGenerator extends AbstractAugmentGenerator {
    ModuleAugmentGenerator(final AugmentEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void loadTargetGenerator() {
        throw new UnsupportedOperationException();
    }

    void linkAugmentationTarget(final GeneratorContext context) {
        // FIXME: do we need two-step resolution here? we probably have solved this somehow, or it's part of...
        // FIXME: MDSAL-696: this looks like the sort of check which should be involved in replacing getOriginal()
        //
        //      if (targetSchemaNode instanceof DataSchemaNode && ((DataSchemaNode) targetSchemaNode).isAddedByUses()) {
        //          if (targetSchemaNode instanceof DerivableSchemaNode) {
        //              targetSchemaNode = ((DerivableSchemaNode) targetSchemaNode).getOriginal().orElse(null);
        //          }
        //          if (targetSchemaNode == null) {
        //              throw new IllegalStateException("Failed to find target node from grouping in augmentation "
        //                  + augSchema + " in module " + context.module().getName());
        //          }
        //      }

        setTargetGenerator(context.resolveSchemaNode(statement().argument()));
    }
}
