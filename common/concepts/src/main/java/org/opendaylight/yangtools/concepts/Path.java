/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Conceptual representation of a logical path in a tree-like structure, similar to a
 * {@link java.nio.file.Path}, but more general in terms of what objects it can be applied to.
 * Paths have an equivalence class, which is captured in the defining type. Paths also have the
 * notion of containment, where one path is said to contain another path if it the data set
 * identified by the former contains all elements of the data set represented by later.
 *
 * @param <P> Path equivalence class
 */
public interface Path<P extends Path<P>> {
    /**
     * Check if this path contains some other.
     *
     * @param other Other path, may not be null.
     * @return True if this path contains the other.
     */
    boolean contains(@NonNull P other);
}
