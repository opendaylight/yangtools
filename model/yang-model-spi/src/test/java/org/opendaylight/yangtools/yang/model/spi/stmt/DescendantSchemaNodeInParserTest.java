/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

class DescendantSchemaNodeInParserTest extends AbstractNamespaceBindingTest {
    @Test
    void happyParseDescendantSchemaNodeid() throws Exception {
        doReturn(BAR).when(namespaceBinding).lookupModule(Unqualified.of("abc"));
        doReturn(FOO).when(namespaceBinding).currentModule();
        assertEquals(Descendant.of(QName.create(BAR, "foolocal"), QName.create(FOO, "barlocal")),
            parsers.descendantSchemaNodeId().parseArgument("abc:foolocal/barlocal"));
    }
}
