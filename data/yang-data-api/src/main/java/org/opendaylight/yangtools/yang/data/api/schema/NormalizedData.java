/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * A piece of data normalized to a particular {@link EffectiveModelContext}. We are making a distinction between
 * {@code data} and {@code metadata} attached to it. This interface captures the former, with two specializations:
 * {@link NormalizedNode} and {@link NormalizedYangData}.
 */
public interface NormalizedData {
    /**
     * Return the contract governing this {@link NormalizedData} instance.
     *
     * @apiNote
     *     This method should be specialized in intermediate contracts like {@link MapNode} and implemented as a default
     *     method by interfaces which form the contracts themselves, for example {@link ContainerNode}, {@link LeafNode}
     *     and similar.
     *
     * @return A class identifying the NormalizedData contract.
     */
    @NonNull Class<? extends NormalizedData> contract();

    /**
     * Return the name of this data.
     *
     * @return Name of this data.
     */
    @NonNull Identifier name();

    /**
     * Returns the body of this node. While the return value specifies {@link Object}, this method's return value has
     * further semantics. The returned object must be a well-published contract, such as {@code String},
     * {@code Collection<NormalizedNode>} or {@code DOMSource}.
     *
     * @return Returned value of this node.
     */
    @NonNull Object body();
}
