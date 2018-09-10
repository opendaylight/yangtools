/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class EffectiveStatementTest {
    @Mock
    private EffectiveStatement1 stmt;
    @Mock
    private EffectiveStatement1 stmt1;
    @Mock
    private Effectivestatement2 stmt2;
    @Mock
    private Map<?, ?> mockNamespace;

    @Before
    public void before() {
        doReturn("one").when(stmt1).argument();
        doReturn("two").when(stmt2).argument();
        doReturn(ImmutableList.of(stmt1, stmt2)).when(stmt).effectiveSubstatements();
        doCallRealMethod().when(stmt).findFirstEffectiveSubstatement(any());
        doCallRealMethod().when(stmt).findFirstEffectiveSubstatementArgument(any());
        doCallRealMethod().when(stmt).streamEffectiveSubstatements(any());
        doCallRealMethod().when(stmt).findAll(any());
    }

    @Test
    public void testFindFirstDeclaredSubstatement() {
        assertEquals(Optional.of(stmt1), stmt.findFirstEffectiveSubstatement(EffectiveStatement1.class));
        assertEquals(Optional.of(stmt2), stmt.findFirstEffectiveSubstatement(Effectivestatement2.class));
    }

    @Test
    public void testFindFirstDeclaredSubstatementArgument() {
        assertEquals(Optional.of("one"), stmt.findFirstEffectiveSubstatementArgument(EffectiveStatement1.class));
        assertEquals(Optional.of("two"), stmt.findFirstEffectiveSubstatementArgument(Effectivestatement2.class));
    }

    @Test
    public void testStreamEffectiveSubstatements() {
        assertEquals(ImmutableList.of(stmt1), stmt.streamEffectiveSubstatements(EffectiveStatement1.class)
            .collect(Collectors.toList()));
        assertEquals(ImmutableList.of(stmt2), stmt.streamEffectiveSubstatements(Effectivestatement2.class)
            .collect(Collectors.toList()));
    }

    @Test
    public void testFindAll() {
        doReturn(mockNamespace).when(stmt).getAll(any());
        assertSame(mockNamespace, stmt.findAll(IdentifierNamespace.class));
    }

    @Test
    public void testFindAllNone() {
        doReturn(null).when(stmt).getAll(any());
        assertEquals(ImmutableMap.of(), stmt.findAll(IdentifierNamespace.class));
    }
}
