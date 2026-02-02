/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Interface for all nodes which are possible targets of augmentation. The
 * target node of augmentation MUST be either a container, list, choice, case,
 * input, output, or notification node.
 */
public interface AugmentationTarget {
    /**
     * Returns augmentations targeting this element.
     *
     * @return set of augmentations targeting this element.
     */
    @NonNull Collection<? extends @NonNull AugmentationSchemaNode> getAvailableAugmentations();

    /**
     * Bridge between {@link EffectiveStatement} and {@link AugmentationTarget}.
     *
     * @param <E> Type of equivalent {@link EffectiveStatement}.
     * @since 15.0.0
     */
    interface Mixin<E extends EffectiveStatement<?, ?>> extends EffectiveStatementEquivalent<E>, AugmentationTarget {
        @Override
        default Collection<? extends AugmentationSchemaNode> getAvailableAugmentations() {
            return asEffectiveStatement().filterEffectiveStatements(AugmentationSchemaNode.class);
        }
    }
}
