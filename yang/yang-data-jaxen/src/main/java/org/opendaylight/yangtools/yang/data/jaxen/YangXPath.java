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
import org.jaxen.FunctionContext;
import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * An {@link org.jaxen.XPath} implementation working on top of {@link NormalizedNode}s.
 */
public final class YangXPath extends BaseXPath {
    private static final long serialVersionUID = 1L;

    YangXPath(final String xpathExpr, final @Nonnull YangNavigator navigator) throws JaxenException {
        super(xpathExpr, Preconditions.checkNotNull(navigator));
    }

    public static YangXPath create(final YangNamespaceContext namespaceContext, final String xpathExpr) throws JaxenException {
        return new YangXPath(xpathExpr, new YangNavigator(namespaceContext));
    }

    @Override
    protected FunctionContext createFunctionContext() {
        return ((YangNavigator)getNavigator()).createFunctionContext();
    }

    @Override
    protected NamespaceContext createNamespaceContext() {
        return ((YangNavigator)getNavigator()).createNamespaceContext();
    }
}
