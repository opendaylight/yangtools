/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

@ExtendWith(MockitoExtension.class)
class YT1414Test {
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private EffectiveModelContext modelContext;
    @Mock
    private ModuleEffectiveStatement module;
    @Mock
    private ContainerEffectiveStatement container;
    @Mock
    private ListEffectiveStatement list;

    @Test
    void testUnsafeOf() {
        final var path = ImmutableList.of(container);
        final var inference = DefaultSchemaTreeInference.unsafeOf(modelContext, path);
        assertSame(modelContext, inference.modelContext());
        assertSame(path, inference.statementPath());
    }

    @Test
    void testVerifiedOf() {
        final var qname = QName.create("foo", "foo");
        doReturn(qname).when(container).argument();

        doReturn(Map.of(qname.getModule(), module)).when(modelContext).namespaceToModule();
        doReturn(Optional.of(container)).when(module).findSchemaTreeNode(qname);

        final var path = ImmutableList.of(container);
        final var inference = DefaultSchemaTreeInference.verifiedOf(modelContext, path);

        assertSame(modelContext, inference.modelContext());
        assertSame(path, inference.statementPath());
    }

    @Test
    void testVerifiedOfNegative() {
        final var qname = QName.create("foo", "foo");
        doReturn(qname).when(container).argument();

        doReturn(Map.of(qname.getModule(), module)).when(modelContext).namespaceToModule();
        doReturn(Optional.of(list)).when(module).findSchemaTreeNode(qname);

        final var ex = assertThrows(IllegalArgumentException.class,
            () -> DefaultSchemaTreeInference.verifiedOf(modelContext, ImmutableList.of(container)));
        assertEquals("Provided path [container] is not consistent with resolved path [list]", ex.getMessage());
    }
}
