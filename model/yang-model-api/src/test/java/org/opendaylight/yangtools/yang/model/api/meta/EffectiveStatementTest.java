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
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EffectiveStatementTest {
    @Mock
    private EffectiveStatement1 stmt;
    @Mock
    private EffectiveStatement1 stmt1;
    @Mock
    private Effectivestatement2 stmt2;
    @Mock
    private Map<?, ?> mockNamespace;

    @BeforeEach
    void before() {
        doReturn(List.of(stmt1, stmt2)).when(stmt).effectiveSubstatements();
    }

    @Test
    void testFindFirstDeclaredSubstatement() {
        doCallRealMethod().when(stmt).findFirstEffectiveSubstatement(any());
        assertEquals(Optional.of(stmt1), stmt.findFirstEffectiveSubstatement(EffectiveStatement1.class));
        assertEquals(Optional.of(stmt2), stmt.findFirstEffectiveSubstatement(Effectivestatement2.class));
    }

    @Test
    void testFindFirstDeclaredSubstatementArgument() {
        doReturn("one").when(stmt1).argument();
        doReturn("two").when(stmt2).argument();
        doCallRealMethod().when(stmt).findFirstEffectiveSubstatement(any());
        doCallRealMethod().when(stmt).findFirstEffectiveSubstatementArgument(any());
        assertEquals(Optional.of("one"), stmt.findFirstEffectiveSubstatementArgument(EffectiveStatement1.class));
        assertEquals(Optional.of("two"), stmt.findFirstEffectiveSubstatementArgument(Effectivestatement2.class));
    }

    @Test
    void testStreamEffectiveSubstatements() {
        doCallRealMethod().when(stmt).streamEffectiveSubstatements(any());
        assertEquals(List.of(stmt1), stmt.streamEffectiveSubstatements(EffectiveStatement1.class).toList());
        assertEquals(List.of(stmt2), stmt.streamEffectiveSubstatements(Effectivestatement2.class).toList());
    }
}
