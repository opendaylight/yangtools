/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementInText;

class ExplicitStatementTest {
    @Test
    void testStatementSource() {
        assertEquals(StatementOrigin.DECLARATION, StatementInText.inFile("foo").statementOrigin());
    }

    @Test
    void testToString() {
        assertEquals("foo", StatementInText.inFile("foo").toString());
        assertEquals("<UNKNOWN>:5:10", StatementInText.atPosition(5, 10).toString());
        assertEquals("foo:5:10", StatementInText.atPosition("foo", 5, 10).toString());
    }
}
