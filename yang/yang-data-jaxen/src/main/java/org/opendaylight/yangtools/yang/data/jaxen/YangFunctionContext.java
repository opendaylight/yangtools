/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Verify;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.FunctionContext;
import org.jaxen.UnresolvableException;
import org.jaxen.XPathFunctionContext;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A {@link FunctionContext} which contains also to YANG-specific current() function.
 */
final class YangFunctionContext implements FunctionContext {
    // Core XPath functions, as per http://tools.ietf.org/html/rfc6020#section-6.4.1
    private static final FunctionContext XPATH_FUNCTION_CONTEXT = new XPathFunctionContext(false);
    // current() function, as per http://tools.ietf.org/html/rfc6020#section-6.4.1
    private static final Function CURRENT_FUNCTION = new Function() {
        @Override
        public NormalizedNode<?, ?> call(final Context context, @SuppressWarnings("rawtypes") final List args) throws FunctionCallException {
            if (!args.isEmpty()) {
                throw new FunctionCallException("current() takes no arguments.");
            }

            Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context %s", context.getClass());
            return ((NormalizedNodeContext) context).getNode();
        }
    };

    // Singleton instance of reuse
    private static final YangFunctionContext INSTANCE = new YangFunctionContext();

    private YangFunctionContext() {
    }

    static YangFunctionContext getInstance() {
        return INSTANCE;
    }

    @Override
    public Function getFunction(final String namespaceURI, final String prefix, final String localName) throws UnresolvableException {
        if (prefix == null && "current".equals(localName)) {
            return CURRENT_FUNCTION;
        }
        return XPATH_FUNCTION_CONTEXT.getFunction(namespaceURI, prefix, localName);
    }
}
