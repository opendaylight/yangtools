/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;

/**
 * Subtree codec specific to model subtree between Java Binding and NormalizedNode.
 */
@Beta
public interface BindingCodecTreeNode {
    /**
     * Return the schema node associated with this node.
     *
     * @return A schema node.
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    @NonNull WithStatus getSchema();
}
