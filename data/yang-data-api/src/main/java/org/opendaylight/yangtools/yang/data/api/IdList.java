/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.NoSuchElementException;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * A {@link List} where each element has a unique identifier. In addition to normal List operations, this interface
 * specifies a number of lookup and modification methods which locate elements by identifier, not index.
 */
@Beta
public interface IdList<I, E extends Identifiable<I>> extends List<E> {
    /**
     * Return the element matching specified identifier, if present.
     *
     * @param id Element identifier
     * @return Matching element, or {@code null}
     * @throws NullPointerException if {@code id} is {@code null}
     */
    @Nullable E lookupElement(I id);

    /**
     * Remove the element matching specified identifier.
     *
     * @param id Element identifier
     * @return Removed element, or {@code null}
     * @throws NullPointerException if {@code id} is {@code null}
     */
    default @Nullable E removeElement(final I id) {
        final var elem = lookupElement(id);
        if (elem != null) {
            verify(remove(elem));
        }
        return elem;
    }

    /**
     * Insert an element to the beginning of this list.
     *
     * @param element Element to insert
     * @throws NullPointerException if {@code element} is {@code null}
     * @throws IllegalArgumentException if there is already an element with the same identifier as {@code element}
     */
    default void insertFirst(final E element) {
        add(0, element);
    }

    /**
     * Insert an element to the end of this list.
     *
     * @param element Element to insert
     * @throws NullPointerException if {@code element} is {@code null}
     * @throws IllegalArgumentException if there is already an element with the same identifier as {@code element}
     */
    default void insertLast(final E element) {
        add(element);
    }

    /**
     * Insert an element after the element with the specified identifier.
     *
     * @param id Identifier of an element existing in this list
     * @param element Element to insert
     * @return Index at which the element was inserted
     * @throws NullPointerException if any argument is {@code null}
     * @throws NoSuchElementException if there is no element matching {@code id}
     * @throws IllegalArgumentException if there is already an element with the same identifier as {@code element}
     */
    int insertAfter(I id, E element);

    /**
     * Insert an element before the element with the specified identifier.
     *
     * @param id Identifier of an element existing in this list
     * @param element Element to insert
     * @return Index at which the element was inserted
     * @throws NullPointerException if any argument is {@code null}
     * @throws NoSuchElementException if there is no element matching {@code id}
     * @throws IllegalArgumentException if there is already an element with the same identifier as {@code element}
     */
    int insertBefore(I id, E element);
}
