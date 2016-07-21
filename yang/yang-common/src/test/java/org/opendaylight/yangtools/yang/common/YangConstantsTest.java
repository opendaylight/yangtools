/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import org.junit.Test;

public class YangConstantsTest {

    @Test
    public void testYangConstants() {
        final URI uriYang = YangConstants.RFC6020_YANG_NAMESPACE;
        final URI uriYin = YangConstants.RFC6020_YIN_NAMESPACE;
        assertEquals(URI.create("urn:ietf:params:xml:ns:yang:1"), uriYang);
        assertEquals(URI.create("urn:ietf:params:xml:ns:yang:yin:1"), uriYin);
        assertEquals(QNameModule.create(uriYang, null).intern(), YangConstants.RFC6020_YANG_MODULE);
        assertEquals(QNameModule.create(uriYin, null).intern(), YangConstants.RFC6020_YIN_MODULE);
    }
}
