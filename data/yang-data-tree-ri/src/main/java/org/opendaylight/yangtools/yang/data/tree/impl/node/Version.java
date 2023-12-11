/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * The concept of a version, either node version, or a subtree version. The only interface contract this class has is
 * that no two {@link Version} are the same.
 *
 * <p>
 * This class relies on Java Virtual machine's guarantee that the identity of an Object is distinct from any other
 * Object in the Java heap.
 *
 * <p>
 * From data management perspective, this concept serves as JVM-level MVCC
 * <a href="https://en.wikipedia.org/wiki/Multiversion_concurrency_control#Implementation">timestamp (TS)</a>.
 */
public final class Version implements Immutable {
    private Version() {
        // Hidden on purpose
    }

    /**
     * Create a new version, distinct from any other version.
     *
     * @return a new version.
     */
    @SuppressWarnings("static-method")
    public @NonNull Version next() {
        return new Version();
    }

    /**
     * Create an initial version.
     *
     * @return a new version.
     */
    public static @NonNull Version initial() {
        return new Version();
    }
}
