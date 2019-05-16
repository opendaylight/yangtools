/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.opaque;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;

/**
 * Opaque structured data. This interface defines an object model usable with {@link AnydataNode} when no underlying
 * schema is available.
 *
 * <p>
 * The data model supports two semantic layouts, based on the source of the data. This is needed due to at least two
 * list encapsulation formats being defined -- JSON encoding (RFC7951) encapsulates list entries in an enclosing list
 * node, while XML encoding (RFC7950) has list entries mixed with other siblings. XML encoding cannot accurately
 * reconstruct this encapsulation because it is impossible to discern a single-node list from a container. The absence
 * of this ambiguity is indicated through {@link #hasAccurateLists()}.
 *
 * <p>
 * All implementations of this interface are required to be deeply-immutable.
 */
@Beta
@NonNullByDefault
public interface OpaqueData extends Immutable {
    /**
     * Get the root node of this data.
     *
     * @return Root node
     */
    OpaqueDataNode getRoot();

    /**
     * Indicate whether the data tree held in this object has accurate list encapsulation or not.
     *
     * @return {@code true} if this tree is guaranteed to use accurate encapsulation of lists, {@code false} otherwise.
     */
    boolean hasAccurateLists();
}
