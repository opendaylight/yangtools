/*
 * Copyright (c) 2014 Robert Varga.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.jaxen.BaseXPath;
import org.jaxen.Context;
import org.jaxen.FunctionContext;
import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * An {@link org.jaxen.XPath} implementation working on top of {@link NormalizedNode}s.
 */
public final class YangXPath extends BaseXPath {
    private static final long serialVersionUID = 1L;

    YangXPath(final String xpathExpr, final @Nonnull NormalizedNodeNavigator navigator) throws JaxenException {
        super(xpathExpr, Preconditions.checkNotNull(navigator));
    }

    @Override
    protected Context getContext(final Object node) {
        if (node instanceof NormalizedNode) {
            return new NormalizedNodeContext(getContextSupport(), (NormalizedNode<?, ?>) node);
        }

        return super.getContext(node);
    }

    @Override
    protected FunctionContext createFunctionContext() {
        // Return proper set of functions
        return YangFunctionContext.getInstance();
    }

    @Override
    protected NamespaceContext createNamespaceContext() {
        // NamespaceContext is bound to a particular module
        return ((NormalizedNodeNavigator)getNavigator()).getNamespaceContext();
    }
}
