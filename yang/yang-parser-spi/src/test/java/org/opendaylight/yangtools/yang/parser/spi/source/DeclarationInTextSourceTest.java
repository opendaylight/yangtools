/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;

public class DeclarationInTextSourceTest {
    @Test
    public void testStatementSource() {
        assertEquals(StatementSource.DECLARATION, DeclarationInTextSource.atLine("foo", 5).getStatementSource());
    }

    @Test
    public void testToString() {
        assertEquals("foo", DeclarationInTextSource.inSource("foo").toString());
        assertEquals("foo:5", DeclarationInTextSource.atLine("foo", 5).toString());
        assertEquals("foo:5:10", DeclarationInTextSource.atPosition("foo", 5, 10).toString());
    }
}
