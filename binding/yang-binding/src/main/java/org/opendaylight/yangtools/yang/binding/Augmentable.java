/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Augmentable (extensible) object which could carry additional data defined by a third-party extension, without
 * introducing conflict between various extension.
 *
 * <p>
 * This interface uses extended version of ExtensibleInterface pattern which also adds marker interface for
 * augmentations (extensions) - {@link Augmentable}.
 *
 * @param <T> Base class which should implements this interface and is target for augmentation.
 * @author Tony Tkacik
 */
public interface Augmentable<T> {
    /**
     * Returns instance of augmentation, if present.
     *
     * @param <A> Type capture for augmentation type
     * @param augmentationType Type of augmentation to be returned
     * @return instance of {@code A}, or {@code null} if the augmentationType is not present
     * @throws NullPointerException if {@code augmentationType} is {@code null}
     */
    default <A extends Augmentation<T>> @Nullable A augmentation(final Class<A> augmentationType) {
        return augmentationType.cast(augmentations().get(augmentationType));
    }

    /**
     * Returns instance of augmentation, or throws {@link NoSuchElementException}.
     *
     * @param <A> Type capture for augmentation type
     * @param augmentationType Type of augmentation to be returned
     * @return An instance of {@code A}
     * @throws NullPointerException if {@code augmentationType} is {@code null}
     * @throws NoSuchElementException if the corresponding augmentation is not present
     *
     * @apiNote
     *     The design here follows {@link Optional#orElseThrow()},
     */
    default <A extends Augmentation<T>> @NonNull A augmentationOrElseThrow(final Class<A> augmentationType) {
        final var augmentation = augmentation(augmentationType);
        if (augmentation != null) {
            return augmentation;
        }
        throw new NoSuchElementException(augmentationType.getName() + " is not present in " + this);
    }

    /**
     * Returns instance of augmentation, or throws {@link NoSuchElementException}.
     *
     * @param <A> Type capture for augmentation type
     * @param <X> Type of the exception to be thrown
     * @param augmentationType Type of augmentation to be returned
     * @param exceptionSupplier the supplying function that produces an exception to be thrown
     * @return An instance of {@code A}
     * @throws NullPointerException if {@code augmentationType} is {@code null}
     * @throws X if the corresponding augmentation is not present
     *
     * @apiNote
     *     The design here follows {@link Optional#orElseThrow(Supplier)},
     */
    default <A extends Augmentation<T>, X extends Throwable> @NonNull A augmentationOrElseThrow(
            final Class<A> augmentationType, final Supplier<@NonNull X> exceptionSupplier) throws X {
        final var augmentation = augmentation(augmentationType);
        if (augmentation != null) {
            return augmentation;
        }
        throw exceptionSupplier.get();
    }

    /**
     * Returns map of all augmentations.
     *
     * @return map of all augmentations.
     */
    @NonNull Map<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations();
}
