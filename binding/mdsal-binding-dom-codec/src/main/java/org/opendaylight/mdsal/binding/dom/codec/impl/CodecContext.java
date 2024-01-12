/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Location specific context for schema nodes, which contains codec specific information to properly serialize
 * and deserialize from Java YANG Binding data to NormalizedNode data.
 *
 * <p>
 * Two core subtypes of codec context are available:
 * <ul>
 *   <li>{@link ValueNodeCodecContext} for nodes, which do not contain any nested YANG modeled substructures</li>
 *   <li>{@link DataContainerCodecContext} for nodes, which contain nested YANG modeled substructures. These context
 *       nodes contain contexts for their individual children nodes</li>
 * </ul>
 */
abstract sealed class CodecContext implements BindingCodecTreeNode
        permits DataContainerCodecContext, ValueNodeCodecContext {
    /**
     * Returns {@link NodeIdentifier} of current node, if applicable.
     *
     * @return NodeIdentifier of node, or {@code null} if not applicable
     */
    abstract @Nullable NodeIdentifier getDomPathArgument();

    /**
     * Return the default value object. Implementations of this method are explicitly allowed to throw unchecked
     * exceptions, which are propagated as-is upwards the stack.
     *
     * @return The default value object, or null if the default value is not defined.
     */
    @Nullable Object defaultObject() {
        return null;
    }

    abstract Object deserializeObject(NormalizedNode normalizedNode);
}
