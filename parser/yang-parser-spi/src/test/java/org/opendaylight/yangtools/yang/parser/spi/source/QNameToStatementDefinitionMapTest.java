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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

@ExtendWith(MockitoExtension.class)
class QNameToStatementDefinitionMapTest {
    private static final QName QNAME = QName.create("", "a");

    private final QNameToStatementDefinitionMap map = new QNameToStatementDefinitionMap();

    @Mock
    private StatementSupport<?, ?, ?> support;

    @Test
    void testPutNullNull() {
        assertThrows(NullPointerException.class, () -> map.put(null, null));
    }

    @Test
    void testPutNullSome() {
        assertThrows(NullPointerException.class, () -> map.put(null, support));
    }

    @Test
    void testPutSomeNull() {
        assertThrows(NullPointerException.class, () -> map.put(QName.create("", "a"), null));
    }

    @Test
    void testPut() {
        doReturn(YangStmtMapping.TYPE).when(support).definition();

        map.put(QNAME, support);
        assertSame(YangStmtMapping.TYPE, map.get(QNAME));
        assertSame(support, map.getSupport(QNAME));
    }
}
