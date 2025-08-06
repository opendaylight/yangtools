/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import com.google.common.annotations.Beta;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Common interface shared between {@link BindingInstanceIdentifier} and {@link DataObjectReference}.
 *
 * @since 14.0.15
 */
@Beta
public sealed interface PathLike permits BindingInstanceIdentifier, DataObjectReference {
    /**
     * Returns the {@link Key} associated with the first component of specified type in this object.
     *
     * @param <E> entry type
     * @param <K> key type
     * @param listItem entry type class
     * @return the {@link Key} associated with the component, or {code null} if the component type is not present
     * @throws NullPointerException if {@code listItem} is {@code null}
     */
    <E extends EntryObject<E, K>, K extends Key<E>> @Nullable K firstKeyOf(@NonNull Class<@NonNull E> listItem);

    /**
     * Returns an {@link Optional} containing the {@link Key} associated with the first component of specified type in
     * this object.
     *
     * @param <E> entry type
     * @param <K> key type
     * @param listItem entry type class
     * @return an optional {@link Key}
     * @throws NullPointerException if {@code listItem} is {@code null}
     */
    default <E extends EntryObject<E, K>, K extends Key<E>> Optional<K> findFirstKeyOf(
            final @NonNull Class<@NonNull E> listItem) {
        return Optional.ofNullable(firstKeyOf(listItem));
    }

    /**
     * Returns the {@link Key} associated with the first component of specified type in this object, throwing
     * {@link NoSuchElementException} if no match is found.
     *
     * @param <E> entry type
     * @param <K> key type
     * @param listItem entry type class
     * @return the {@link Key} associated with the component
     * @throws NullPointerException if {@code listItem} is {@code null}
     * @throws NoSuchElementException if the component type is not present
     */
    default <E extends EntryObject<E, K>, K extends Key<E>> @NonNull K getFirstKeyOf(
            final @NonNull Class<@NonNull E> listItem) {
        final var key = firstKeyOf(listItem);
        if (key != null) {
            return key;
        }
        throw new NoSuchElementException("No key matching " + listItem.getName() + " found in " + this);
    }
}
