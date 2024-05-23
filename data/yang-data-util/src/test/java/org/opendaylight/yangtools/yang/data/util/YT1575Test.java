/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1575Test {
    @Test
    void testMapEntryRoot() throws Exception {
        final var modelContext = YangParserTestUtils.parseYang("""
            module yt1575 {
              namespace yt1575;
              prefix yt1575;

              container testContainer {
                list testList {
                  key "name";
                  leaf name {
                    type string;
                  }
                }
              }
            }""");

        final var testContainer = QName.create("yt1575", "testContainer");
        final var testList = QName.create("yt1575", "testList");

        final var stack = NormalizedNodeStreamWriterStack.of(modelContext, Absolute.of(testContainer, testList));
        assertNull(stack.currentStatement());
        stack.startListItem(new NodeIdentifier(testList));
        assertInstanceOf(ListEffectiveStatement.class, stack.currentStatement());
    }
}
