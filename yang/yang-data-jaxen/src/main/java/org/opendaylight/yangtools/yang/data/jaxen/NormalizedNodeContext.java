/*
 * Copyright (c) 2015 Robert Varga.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Preconditions;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Context wrapper around a {@link NormalizedNode} for use with Jaxen.
 */
final class NormalizedNodeContext extends Context {
    private static final long serialVersionUID = 1L;
    private final NormalizedNode<?, ?> node;

    NormalizedNodeContext(final ContextSupport contextSupport, final NormalizedNode<?, ?> node) {
        super(contextSupport);
        this.node = Preconditions.checkNotNull(node);
    }

    NormalizedNode<?, ?> getNormalizedNode() {
        return node;
    }
}
