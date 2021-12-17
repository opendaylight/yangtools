/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DeclaredStatementTest {
    @Mock
    public DeclaredStatement1 stmt;
    @Mock
    public DeclaredStatement1 stmt1;
    @Mock
    public DeclaredStatement2 stmt2;

    @Before
    public void before() {
        doReturn(List.of(stmt1, stmt2)).when(stmt).declaredSubstatements();
        doCallRealMethod().when(stmt).declaredSubstatements(any());
        doCallRealMethod().when(stmt).findFirstDeclaredSubstatement(any());
        doCallRealMethod().when(stmt).streamDeclaredSubstatements(any());
    }

    @Test
    public void testDeclaredSubstatements() {
        assertEquals(List.of(stmt1), List.copyOf(stmt.declaredSubstatements(DeclaredStatement1.class)));
        assertEquals(List.of(stmt2), List.copyOf(stmt.declaredSubstatements(DeclaredStatement2.class)));
    }

    @Test
    public void testFindFirstDeclaredSubstatement() {
        assertEquals(Optional.of(stmt1), stmt.findFirstDeclaredSubstatement(DeclaredStatement1.class));
        assertEquals(Optional.of(stmt2), stmt.findFirstDeclaredSubstatement(DeclaredStatement2.class));
    }
}
