/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement1;

@ExtendWith(MockitoExtension.class)
class ActionStatementAwareDeclaredStatementTest {
    @Mock
    ActionStatementAwareDeclaredStatement<?> stmt;
    @Mock
    DeclaredStatement1 stmt1;
    @Mock
    ActionStatement stmt2;

    @BeforeEach
    void before() {
        doReturn(ImmutableList.of(stmt1, stmt2)).when(stmt).declaredSubstatements();
        doCallRealMethod().when(stmt).declaredSubstatements(any());
        doCallRealMethod().when(stmt).getActions();
    }

    @Test
    void testGetActions() {
        assertEquals(ImmutableList.of(stmt2), ImmutableList.copyOf(stmt.getActions()));
    }
}
