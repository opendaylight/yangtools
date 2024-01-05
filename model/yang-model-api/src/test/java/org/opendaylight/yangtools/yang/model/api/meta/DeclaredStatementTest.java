/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeclaredStatementTest {
    @Mock
    private DeclaredStatement1 stmt;
    @Mock
    private DeclaredStatement1 stmt1;
    @Mock
    private DeclaredStatement2 stmt2;

    @BeforeEach
    void before() {
        doReturn(List.of(stmt1, stmt2)).when(stmt).declaredSubstatements();
    }

    @Test
    void testDeclaredSubstatements() {
        doCallRealMethod().when(stmt).declaredSubstatements(any());
        assertEquals(List.of(stmt1), List.copyOf(stmt.declaredSubstatements(DeclaredStatement1.class)));
        assertEquals(List.of(stmt2), List.copyOf(stmt.declaredSubstatements(DeclaredStatement2.class)));
    }

    @Test
    void testFindFirstDeclaredSubstatement() {
        doCallRealMethod().when(stmt).streamDeclaredSubstatements(any());
        doCallRealMethod().when(stmt).findFirstDeclaredSubstatement(any());
        assertEquals(Optional.of(stmt1), stmt.findFirstDeclaredSubstatement(DeclaredStatement1.class));
        assertEquals(Optional.of(stmt2), stmt.findFirstDeclaredSubstatement(DeclaredStatement2.class));
    }

    @Test
    void testFindFirstDeclaredSubstatementArgument() {
        doReturn("one").when(stmt1).argument();
        doReturn("two").when(stmt2).argument();
        doCallRealMethod().when(stmt).streamDeclaredSubstatements(any());
        doCallRealMethod().when(stmt).findFirstDeclaredSubstatement(any());
        doCallRealMethod().when(stmt).findFirstDeclaredSubstatementArgument(any());
        assertEquals(Optional.of("one"), stmt.findFirstDeclaredSubstatementArgument(DeclaredStatement1.class));
        assertEquals(Optional.of("two"), stmt.findFirstDeclaredSubstatementArgument(DeclaredStatement2.class));
    }
}
