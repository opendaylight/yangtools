/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.impl.AbstractDataObjectReference;
import org.opendaylight.yangtools.binding.impl.DataObjectReferenceImpl;
import org.opendaylight.yangtools.binding.impl.DataObjectReferenceWithKey;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Builder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.KeyedBuilder;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * A reference to a {@link DataObject} with semantics partially overlapping with to YANG {@code instance-identifier}.
 *
 * <p>
 * While this indirection is not something defined in YANG, this class hierarchy arises naturally from the Binding
 * specification's Java footprint, which uses {@link DataObject} as the baseline self-sufficient addressable construct.
 * This means users can use a {@link KeyAware} class without specifying the corresponding key -- resulting in an
 * {@link InexactDataObjectStep}.
 *
 * @param <T> type of {@link DataObject} held in the last step.
 */
// FIXME: YANGTOOLS-1577: revise this bit of documentation
//*
//* <p>
//* There are two kinds of a reference based on their treatment of such a {@link InexactDataObjectStep}:
//* <ul>
//*   <li>{@link DataObjectIdentifier}, which accepts only {@link ExactDataObjectStep}s and represents
//*       a {@link BindingInstanceIdentifier} pointing to a {@link DataObject}</li>
//*   <li>{@link DataObjectWildcard}, which accepts any {@link DataObjectStep} and represents path-based matching
//*       criteria for one or more {@link DataObjectIdentifier}s based on
//*       {@link InexactDataObjectStep#matches(DataObjectStep)}.
//* </ul>
public sealed interface DataObjectReference<T extends DataObject> extends Immutable, Serializable
        permits DataObjectIdentifier, DataObjectReference.WithKey, AbstractDataObjectReference {
    /**
     * A {@link DataObjectReference} pointing to a {@link KeyAware} {@link DataObject}, typically a map entry.
     *
     * @param <K> Key type
     * @param <T> KeyAware type
     */
    sealed interface WithKey<T extends KeyAware<K> & DataObject, K extends Key<T>>
            extends DataObjectReference<T>, KeyAware<K>
            permits DataObjectIdentifier.WithKey, DataObjectReferenceWithKey, KeyedInstanceIdentifier {
        @Override
        KeyedBuilder<T, K> toBuilder();

        /**
         * Return the key attached to this identifier. This method is equivalent to calling
         * {@link InstanceIdentifier#keyOf(InstanceIdentifier)}.
         *
         * @return Key associated with this instance identifier.
         * @deprecated Use {@link #key()} instead.
         */
        @Deprecated(since = "14.0.0")
        default @NonNull K getKey() {
            return key();
        }
    }

    static @NonNull DataObjectReference<?> ofUnsafeSteps(final Iterable<? extends @NonNull DataObjectStep<?>> steps) {
        return ofUnsafeSteps(ImmutableList.copyOf(steps));
    }

    static @NonNull DataObjectReference<?> ofUnsafeSteps(final List<? extends @NonNull DataObjectStep<?>> steps) {
        return ofUnsafeSteps(ImmutableList.copyOf(steps));
    }

    static @NonNull DataObjectReference<?> ofUnsafeSteps(
            final ImmutableList<? extends @NonNull DataObjectStep<?>> steps) {
        return DataObjectReferenceImpl.ofUnsafeSteps(steps);
    }

    /**
     * Return the steps of this reference. Returned {@link Iterable} does not support removals and contains one or more
     * non-null items.
     *
     * @return the steps of this reference
     */
    @NonNull Iterable<? extends @NonNull DataObjectStep<?>> steps();

    /**
     * Create a new {@link Builder} initialized to produce a reference equal to this one.
     *
     * @return A builder instance
     */
    @NonNull Builder<T> toBuilder();

    /**
     * Returns {@code true} if this reference is composed solely of {@link ExactDataObjectStep}s.
     *
     * @implNote
     *     The default implementation returns {@code false} to simplify implementation hierarchy.
     * @return {@code true} if this reference is composed solely of {@link ExactDataObjectStep}s
     */
    default boolean isExact() {
        return false;
    }

    /**
     * Create a new {@link Builder} initialized to produce a reference equal to this one.
     *
     * @return A builder instance
     * @deprecated Use {@link #toBuilder()} instead.
     */
    @Deprecated(since = "14.0.0")
    default @NonNull Builder<T> builder() {
        return toBuilder();
    }

    /**
     * Return the steps of this reference. Returned {@link Iterable} does not support removals and contains one or more
     * non-null ite,s.
     *
     * @return the steps of this reference
     * @deprecated Use {@link #steps()} instead.
     */
    @Deprecated(since = "14.0.0")
    default @NonNull Iterable<? extends @NonNull DataObjectStep<?>> getPathArguments() {
        return steps();
    }

    /**
     * Returns {@code true} if this reference contains an {@link InexactDataObjectStep}s.
     *
     * @implSpec
     *     The default implementation returns {@code true} to simplify implementation hierarchy.
     * @return {@code true} if this reference contains an {@link InexactDataObjectStep}
     * @deprecated Use negated result of {@link #isExact()} instead
     */
    @Deprecated(since = "14.0.0")
    default boolean isWildcarded() {
        return true;
    }
}
