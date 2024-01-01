/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

@ExtendWith(MockitoExtension.class)
class YT1414Test {
    @Mock
    EffectiveModelContext modelContext;
    @Mock
    ContainerEffectiveStatement container;

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

        final var module = mock(ModuleEffectiveStatement.class);
        doReturn(Optional.of(module)).when(modelContext).findModuleStatement(qname.getModule());
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

        final var module = mock(ModuleEffectiveStatement.class);
        doReturn(Optional.of(module)).when(modelContext).findModuleStatement(qname.getModule());
        doReturn(Optional.of(mock(ListEffectiveStatement.class))).when(module).findSchemaTreeNode(qname);

        assertThat(assertThrows(IllegalArgumentException.class,
            () -> DefaultSchemaTreeInference.verifiedOf(modelContext, ImmutableList.of(container)))
            .getMessage(), startsWith(
                "Provided path [container] is not consistent with resolved path [Mock for ListEffectiveStatement, "));
    }
}
