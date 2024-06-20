/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentableRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Abstract base class for {@link AbstractCompositeRuntimeType}s which support augmentations.
 */
abstract class AbstractAugmentableRuntimeType<S extends EffectiveStatement<?, ?>>
        extends AbstractCompositeRuntimeType<S> implements AugmentableRuntimeType {
    private final @NonNull ImmutableList<AugmentRuntimeType> augments;

    AbstractAugmentableRuntimeType(final GeneratedType bindingType, final S statement, final List<RuntimeType> children,
            final List<AugmentRuntimeType> augments) {
        super(bindingType, statement, children);
        this.augments = ImmutableList.copyOf(augments);
    }

    @Override
    public final List<AugmentRuntimeType> augments() {
        return augments;
    }
}
