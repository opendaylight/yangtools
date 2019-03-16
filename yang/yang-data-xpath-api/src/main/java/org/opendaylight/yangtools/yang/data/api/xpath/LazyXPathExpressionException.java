/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.xpath;

import com.google.common.annotations.Beta;
import javax.xml.xpath.XPathExpressionException;

/**
 * The runtime counterpart of {@link XPathExpressionException}. Can occur only when the user is accessing any state
 * created from the user's invocation to the {@link LazyXPathExpression} API.
 *
 * @deprecated PREVIEW API. DO NOT IMPLEMENT YET AS THIS NEEDS TO BE VALIDATED FOR USE IN CLIENT APPLICATIONS.
 *             APPLICATIONS WILLING TO USE THIS API PLEASE CONTACT
 *             <a href="mailto:yangtools-dev@lists.opendaylight.org">yangtools-dev</a>.
 */
@Beta
@Deprecated
public class LazyXPathExpressionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LazyXPathExpressionException(final String message) {
        super(message);
    }

    public LazyXPathExpressionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
