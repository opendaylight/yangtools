/*
 * Copyright (c) 2014 Robert Varga.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.jaxen.FunctionContext;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeUtils;

final class YangFunctionContextFactory {
    private static final FunctionContext NULL_FUNCTION_CONTEXT = new YangFunctionContext(new CurrentFunction(null));
    private final YangInstanceIdentifier nodePath;

    YangFunctionContextFactory(final YangInstanceIdentifier nodePath) {
        this.nodePath = Preconditions.checkNotNull(nodePath);
    }

    public FunctionContext createContext(final NormalizedNodeContainer<?, ?, ?> root) {
        final Optional<NormalizedNode<?, ?>> node = NormalizedNodeUtils.findNode(root, nodePath);
        if (node.isPresent()) {
            return new YangFunctionContext(new CurrentFunction(node.get()));
        } else {
            return NULL_FUNCTION_CONTEXT;
        }
    }
}
