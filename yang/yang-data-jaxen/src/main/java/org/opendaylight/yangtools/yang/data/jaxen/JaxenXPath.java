/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Preconditions;
import org.jaxen.JaxenException;

public final class JaxenXPath {
    private final YangXPath xpath;

    JaxenXPath(final YangXPath xpath) {
        this.xpath = Preconditions.checkNotNull(xpath);
    }

    Object evaluate(final NormalizedNodeContext context) throws JaxenException {
        return xpath.evaluate(context);
    }

}
