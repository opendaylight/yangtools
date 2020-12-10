/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;

public class YT1201Test {
    private static final QName FOO = QName.create("urn:foo", "foo");

    @Test
    public void testWhenPrefixes() throws Exception {
        StmtTestUtils.parseYangSources("/bugs/YT1201/");
    }
}
