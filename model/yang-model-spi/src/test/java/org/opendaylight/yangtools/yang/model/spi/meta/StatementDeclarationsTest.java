/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;

class StatementDeclarationsTest {
    @Test
    void testStatementSource() {
        assertEquals(StatementOrigin.DECLARATION, StatementDeclarations.inText(1, 1).statementOrigin());
    }

    @Test
    void testToString() {
        assertEquals("<UNKNOWN>:5:10", StatementDeclarations.inText(5, 10).toString());
        assertEquals("foo:5:10", StatementDeclarations.inText("foo", 5, 10).toString());
        assertEquals("<UNKNOWN>:1:1", StatementDeclarations.inText(null, 1, 1).toString());
    }
}
