/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;

public class Bug4969Test {
    @Test
    public void testRegex() {
        RevisionAwareXPath xpath = new RevisionAwareXPathImpl(
                "nd:network[nd:network-id=current()/../network-ref]/nd:node[nd:node-id=current()/../node-ref]"
                + "/termination-point/tp-id", true);
        assertEquals("nd:network/nd:node/termination-point/tp-id",
                SchemaContextUtil.stripConditionsFromXPathString(xpath));
    }
}
