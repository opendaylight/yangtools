/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

public class QNameToStatementDefinitionMapTest {
    private static final QName QNAME = QName.create("", "a");

    private final QNameToStatementDefinitionMap map = new QNameToStatementDefinitionMap();
    private final StatementSupport<?, ?, ?> support = mock(StatementSupport.class);

    @Test
    public void testPutNullNull() {
        assertThrows(NullPointerException.class, () -> map.put(null, null));
    }

    @Test
    public void testPutNullSome() {
        assertThrows(NullPointerException.class, () -> map.put(null, mock(StatementSupport.class)));
    }

    @Test
    public void testPutSomeNull() {
        assertThrows(NullPointerException.class, () -> map.put(QName.create("", "a"), null));
    }

    @Test
    public void testPut() {
        map.put(QNAME, support);
        assertSame(support, map.get(QNAME));
    }
}
