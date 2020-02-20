/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.binding.runtime.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;

/**
 * Runtime Context for Java YANG Binding classes. It provides information derived from the backing effective model,
 * which is not captured in generated classes (and hence cannot be obtained from {@code BindingReflections}.
 *
 * <p>Some of this information are for example list of all available children for cases
 * {@link #getChoiceCaseChildren(DataNodeContainer)}, since choices are augmentable and new choices may be introduced
 * by additional models. Same goes for all possible augmentations.
 */
@Beta
public final class DefaultBindingRuntimeContext extends AbstractBindingRuntimeContext {
    private final @NonNull BindingRuntimeTypes runtimeTypes;
    private final @NonNull ClassLoadingStrategy strategy;

    private DefaultBindingRuntimeContext(final BindingRuntimeTypes runtimeTypes, final ClassLoadingStrategy strategy) {
        this.runtimeTypes = requireNonNull(runtimeTypes);
        this.strategy = requireNonNull(strategy);
    }

    /**
     * Creates Binding Runtime Context from supplied class loading strategy and schema context.
     *
     * @param strategy Class loading strategy to retrieve generated Binding classes
     * @param runtimeTypes Binding classes to YANG schema mapping
     * @return A new instance
     */
    public static @NonNull DefaultBindingRuntimeContext create(final BindingRuntimeTypes runtimeTypes,
            final ClassLoadingStrategy strategy) {
        return new DefaultBindingRuntimeContext(runtimeTypes, strategy);
    }

    /**
     * Returns a class loading strategy associated with this binding runtime context
     * which is used to load classes.
     *
     * @return Class loading strategy.
     */
    @Override
    public @NonNull ClassLoadingStrategy getStrategy() {
        return strategy;
    }

    @Override
    public @NonNull BindingRuntimeTypes getTypes() {
        return runtimeTypes;
    }
}
