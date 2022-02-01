/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import java.io.File;
import org.junit.Test;

public class YT1393Test {
    @Test
    public void testSameModuleWrongUnique() throws Exception {
        StmtTestUtils.parseYangSources(
            new File(YT1393Test.class.getResource("/bugs/YT1393/ietf-tls-server.yang").toURI()),
            new File(YT1393Test.class.getResource("/bugs/YT1393/ietf-netconf-server.yang").toURI()));
    }
}
