/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement1;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ActionStatementAwareDeclaredStatementTest {
    @Mock
    public ActionStatementAwareDeclaredStatement<?> stmt;
    @Mock
    public DeclaredStatement1 stmt1;
    @Mock
    public ActionStatement stmt2;

    @Before
    public void before() {
        doReturn(ImmutableList.of(stmt1, stmt2)).when(stmt).declaredSubstatements();
        doCallRealMethod().when(stmt).declaredSubstatements(any());
        doCallRealMethod().when(stmt).getActions();
    }

    @Test
    public void testGetActions() {
        assertEquals(ImmutableList.of(stmt2), ImmutableList.copyOf(stmt.getActions()));
    }
}
