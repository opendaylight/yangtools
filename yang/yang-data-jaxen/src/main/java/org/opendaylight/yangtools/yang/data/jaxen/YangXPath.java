/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import org.jaxen.BaseXPath;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * An {@link org.jaxen.XPath} implementation working on top of {@link NormalizedNode}s.
 */
public final class YangXPath extends BaseXPath {
    private static final long serialVersionUID = 1L;

    YangXPath(final String xpathExpr) throws JaxenException {
        super(xpathExpr);
    }

    @Override
    protected ContextSupport getContextSupport() {
        throw new UnsupportedOperationException();
    }
}
