/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class Bug2291Test {
    @Test
    void testRevisionWithExt() throws Exception {
        assertNotNull(TestUtils.parseYangSource("/bugs/bug2291/bug2291-ext.yang", "/bugs/bug2291/bug2291.yang",
            "/ietf/ietf-inet-types@2010-09-24.yang"));
    }
}
