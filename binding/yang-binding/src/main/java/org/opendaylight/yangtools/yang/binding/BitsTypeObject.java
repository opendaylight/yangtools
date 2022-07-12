/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Interface implemented by all {@link TypeObject}s generated for {@code type bits}.
 */
public interface BitsTypeObject extends TypeObject {
    /**
     * Return the set of strings which are valid {@code bit} names for this type. The iteration order of the returned
     * set is required to match effective bit {@code position} assignment.
     *
     * @return The names of valid bits for this type.
     */
    @NonNull ImmutableSet<String> validNames();

    /**
     * Return the bit values. Returned array contains all bits from {@link #validNames()} in the interation order of
     * that set. Note that the array is packed, e.g. unassigned positions are skipped over.
     *
     * @return Array of values.
     */
    boolean @NonNull [] values();
}
