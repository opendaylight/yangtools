/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2021 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

/**
 * An {@link Identifier} tied to some tree-like structure, similar to how {@link java.nio.file.Path} is tied to a
 * conceptual file system. In addition to equivalence class implied by Identifier, the hierarchical nature of these
 * identifiers also introduces a notion of containment: a HierarchicalIdentifier is said to contain another
 * HierarchicalIdentifier if the former points to an ancestor node of the node pointed to by the latter in the same
 * instance of the tree-like structure they are defined on. This property is expressed through
 * {@link #contains(HierarchicalIdentifier)}.
 */
public interface HierarchicalIdentifier<T extends HierarchicalIdentifier<T>> extends Identifier {
    /**
     * Check if this identifier contains some other identifier. If we take HierarchicalIdentifier to be similar to a
     * {@link java.nio.file.Path}, this is method is the equivalent of {@code other.startsWith(this)}.
     *
     * @param other Other identifier, may not be {@code null}
     * @return True if this identifier contains the other identifier
     * @throws NullPointerException if {@code other} is {@code null}
     */
    boolean contains(T other);
}
