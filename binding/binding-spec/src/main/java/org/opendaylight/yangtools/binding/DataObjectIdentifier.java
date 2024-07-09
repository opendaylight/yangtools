/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.impl.AbstractDataObjectIdentifierBuilder;
import org.opendaylight.yangtools.binding.impl.DataObjectIdentifierImpl;
import org.opendaylight.yangtools.binding.impl.DataObjectIdentifierWithKey;

/**
 * A {@link DataObjectReference} matching at most one {@link DataObject}, consistent with YANG
 * {@code instance-identifier} addressing as captured by {@link BindingInstanceIdentifier}.
 *
 * @param <T> type of {@link DataObject} held in the last step.
 */
public sealed interface DataObjectIdentifier<T extends DataObject>
        extends DataObjectReference<T>, BindingInstanceIdentifier
        permits DataObjectIdentifier.WithKey, DataObjectIdentifierImpl {
    /**
     * A builder of {@link DataObjectReference} objects.
     *
     * @param <T> type of {@link DataObject} held in the last step.
     */
    sealed interface Builder<T extends DataObject> extends DataObjectReference.Builder<T>
            permits Builder.WithKey, AbstractDataObjectIdentifierBuilder {
        /**
         * A builder of {@link DataObjectReference.WithKey} objects.
         *
         * @param <T> type of {@link EntryObject} held in the last step.
         * @param <K> {@link Key} type
         */
        non-sealed interface WithKey<T extends EntryObject<T, K>, K extends Key<T>>
                extends Builder<T>, DataObjectReference.Builder.WithKey<T, K>
                /* permits DataObjectReferenceBuilderWithKey, KeyedBuilder */ {
            @Override
            DataObjectIdentifier.WithKey<T, K> build();
        }

        @Override
        DataObjectIdentifier<T> build();
    }

    /**
     * A {@link DataObjectIdentifier} pointing to an {@link EntryObject}.
     *
     * @param <K> Key type
     * @param <T> EntryObject type
     */
    sealed interface WithKey<T extends EntryObject<T, K>, K extends Key<T>>
            extends DataObjectIdentifier<T>, DataObjectReference.WithKey<T, K>
            permits DataObjectIdentifierWithKey {
        @Override
        KeyStep<K, T> lastStep();

        @Override
        DataObjectIdentifier.Builder.WithKey<T, K> toBuilder();
    }

    static @NonNull DataObjectIdentifier<?> ofUnsafeSteps(
            final Iterable<? extends @NonNull ExactDataObjectStep<?>> steps) {
        return ofUnsafeSteps(ImmutableList.copyOf(steps));
    }

    static @NonNull DataObjectIdentifier<?> ofUnsafeSteps(
            final List<? extends @NonNull ExactDataObjectStep<?>> steps) {
        return ofUnsafeSteps(ImmutableList.copyOf(steps));
    }

    static @NonNull DataObjectIdentifier<?> ofUnsafeSteps(
            final ImmutableList<? extends @NonNull ExactDataObjectStep<?>> steps) {
        return DataObjectIdentifierImpl.ofUnsafeSteps(steps);
    }

    @Override
    Iterable<? extends @NonNull ExactDataObjectStep<?>> steps();

    @Override
    default boolean isExact() {
        return true;
    }

    @Override
    @Deprecated(since = "14.0.0")
    default boolean isWildcarded() {
        return false;
    }
}
