/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

@ExtendWith(MockitoExtension.class)
class ReactorStatementDefinitionResolverTest {
    private static final QName QNAME = QName.create("", "a");

    private final ReactorStatementDefinitionResolver resolver = new ReactorStatementDefinitionResolver();

    @Mock
    private StatementSupport<?, ?, ?> support;

    @Test
    void testPutNullNull() {
        assertThrows(NullPointerException.class, () -> resolver.addSupport(null, null));
    }

    @Test
    void testPutNullSome() {
        assertThrows(NullPointerException.class, () -> resolver.addSupport(null, support));
    }

    @Test
    void testPutSomeNull() {
        assertThrows(NullPointerException.class, () -> resolver.addSupport(QName.create("", "a"), null));
    }

    @Test
    void testPut() {
        doReturn(TypeStatement.DEF).when(support).definition();

        resolver.addSupports(Map.of(QNAME, support));
        assertSame(TypeStatement.DEF, resolver.lookupDef(QNAME.getModule(), QNAME.getLocalName()));
        assertSame(support, resolver.lookupStatementSupport(QNAME));
    }
}
