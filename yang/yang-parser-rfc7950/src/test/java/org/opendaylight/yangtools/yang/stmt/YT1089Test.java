/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class YT1089Test {
    @Test
    public void testPlusLexing() throws Exception {
        final SchemaContext ctx = StmtTestUtils.parseYangSource("/bugs/YT1089/foo.yang");
    }
}
