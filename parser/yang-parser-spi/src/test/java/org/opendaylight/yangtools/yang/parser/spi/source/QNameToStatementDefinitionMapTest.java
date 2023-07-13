/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

public class QNameToStatementDefinitionMapTest {
    private static final QName QNAME = QName.create("", "a");

    private final QNameToStatementDefinitionMap map = new QNameToStatementDefinitionMap();
    private final StatementSupport<?, ?, ?> support = mock(StatementSupport.class);
    private final StatementDefinition definition = mock(StatementDefinition.class);

    @BeforeEach
    public void before() {
        doReturn(definition).when(support).definition();
    }

    @Test
    public void testPutNullNull() {
        assertThrows(NullPointerException.class, () -> map.put(null, null));
    }

    @Test
    public void testPutNullSome() {
        assertThrows(NullPointerException.class, () -> map.put(null, support));
    }

    @Test
    public void testPutSomeNull() {
        assertThrows(NullPointerException.class, () -> map.put(QName.create("", "a"), null));
    }

    @Test
    public void testPut() {
        map.put(QNAME, support);
        assertSame(definition, map.get(QNAME));
        assertSame(support, map.getSupport(QNAME));
    }
}
