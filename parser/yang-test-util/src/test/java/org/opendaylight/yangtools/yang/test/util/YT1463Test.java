/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.test.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;

public class YT1463Test {
    @Test
    public void testParseYang() {
        final var fromLiteral = YangParserTestUtils.parseYang("""
            module root-conflict { // results in RootConflictData
              namespace root-conflict;
              prefix rc;

              container root-conflict-data; // results in RootConflictData as well
            }""");

        final var fromResources = YangParserTestUtils.parseYangResourceDirectory("/");
        final var qname = QName.create("root-conflict", "root-conflict");
        assertTrue(fromResources.findModule(qname.getModule()).isPresent());
        assertTrue(fromLiteral.findModule(qname.getModule()).isPresent());
    }
}
