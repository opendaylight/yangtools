/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 *
 * Generic serializer for normalized nodes. NormalizedNodes can be serialized
 * e.g. to Dom APIs.
 *
 * @param <E>
 *            type of resulting/serialized element from NormalizedNode
 * @param <N>
 *            type of NormalizedNode to be serialized
 * @param <S>
 *            schema belonging to the type N of NormalizedNode
 *
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public interface FromNormalizedNodeSerializer<E, N extends NormalizedNode<?, ?>, S> {

    /**
     *
     * Serialize one node of type N as a list of E elements. If the serialization
     * process creates only one E element as a result e.g. container node, the
     * result element is expected to be wrapped in a list.
     *
     * @param schema schema belonging to the type N of NormalizedNode
     * @param node NormalizedNode to be serialized
     * @return Serialized N node as a list of E elements
     */
    Iterable<E> serialize(S schema, N node);
}
