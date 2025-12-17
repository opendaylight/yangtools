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
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

class AbsoluteSchemaNodeInParserTest extends AbstractNamespaceBindingTest {
    @Test
    void happyParseArgument() throws Exception {
        doReturn(FOO).when(namespaceBinding).lookupModule(Unqualified.of("abc"));
        doReturn(BAR).when(namespaceBinding).currentModule();
        assertEquals(Absolute.of(QName.create(FOO, "foolocal"), QName.create(BAR, "barlocal")),
            parsers.absoluteSchemaNodeId().parseArgument("/abc:foolocal/barlocal"));
    }
}
