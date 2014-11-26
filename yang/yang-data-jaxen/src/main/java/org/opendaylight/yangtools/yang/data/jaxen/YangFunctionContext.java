/*
 * Copyright (c) 2014 Robert Varga.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Preconditions;
import org.jaxen.Function;
import org.jaxen.FunctionContext;
import org.jaxen.UnresolvableException;
import org.jaxen.XPathFunctionContext;

final class YangFunctionContext implements FunctionContext {
    // Core XPath functions, as per http://tools.ietf.org/html/rfc6020#section-6.4.1
    private static final FunctionContext XPATH_FUNCTION_CONTEXT = new XPathFunctionContext(false);
    private final Function current;

    YangFunctionContext(final Function current) {
        this.current = Preconditions.checkNotNull(current);
    }

    @Override
    public Function getFunction(final String namespaceURI, final String prefix, final String localName) throws UnresolvableException {
        if (prefix == null && "current".equals(localName)) {
            return current;
        }
        return XPATH_FUNCTION_CONTEXT.getFunction(namespaceURI, prefix, localName);
    }
}
