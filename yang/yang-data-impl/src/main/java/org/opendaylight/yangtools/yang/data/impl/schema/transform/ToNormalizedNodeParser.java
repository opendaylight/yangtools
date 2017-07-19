/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 *
 * Generic parser for normalized nodes. NormalizedNodes can be parsed
 * e.g. from Dom APIs.
 *
 * @param <E>
 *            type of element to be parsed into NormalizedNode
 * @param <N>
 *            type of NormalizedNode to be the result of parsing
 * @param <S>
 *            schema belonging to the type N of NormalizedNode
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public interface ToNormalizedNodeParser<E, N extends NormalizedNode<?, ?>, S> {

    /**
     *
     * Parse a list of E elements as a NormalizedNode of type N. If the parsing
     * process expects only one E element as input e.g. container node, the
     * input element will be wrapped in a list.
     *
     * @param elements elements to be parsed into NormalizedNode
     * @param schema schema belonging to the type N of NormalizedNode
     * @return NormalizedNode as a result of parsing list of E elements with schema S
     */
    @Nullable
    N parse(Iterable<E> elements, S schema);
}
