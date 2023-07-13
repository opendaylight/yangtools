/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;

class ImplicitStatementTest {
    @Test
    void testForwarding() {
        final var ref = mock(StatementSourceReference.class);
        final var stmt = ImplicitSubstatement.of(ref);
        assertEquals(StatementOrigin.CONTEXT, stmt.statementOrigin());

        doReturn("ref").when(ref).toString();
        assertEquals("ref", stmt.toString());
    }
}
