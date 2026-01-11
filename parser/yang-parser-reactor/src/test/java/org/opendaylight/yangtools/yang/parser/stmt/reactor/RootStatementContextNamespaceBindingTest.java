/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@ExtendWith(MockitoExtension.class)
class RootStatementContextNamespaceBindingTest {
    private static final @NonNull QNameModule FOO_NS = QNameModule.ofRevision("foo", "2025-12-27");
    private static final @NonNull Unqualified FOO = Unqualified.of("foo");

    @Mock
    private RootStatementContext<?, ?, ?> context;
    @Mock
    private StmtContext<?, ?, ?> importedContext;

    @BeforeEach
    void beforeEach() {
        doReturn(FOO.bindTo(FOO_NS)).when(context).moduleName();
        doCallRealMethod().when(context).newNamespaceBinding();
    }

    @Test
    void currentModuleInvokesDefiningModule() {
        doReturn(Map.of()).when(context).namespace(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX);
        doReturn(false).when(context).producesDeclared(SubmoduleStatement.class);

        final var namespaceBinding = context.newNamespaceBinding();
        assertSame(FOO_NS, namespaceBinding.currentModule());
        assertSame(FOO_NS, namespaceBinding.currentModule());
        verify(context).namespace(any());
    }

    @Test
    void lookupModuleImported() {
        doReturn(Map.of("foo", importedContext)).when(context).namespace(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX);
        doReturn(FOO_NS).when(context).namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, importedContext);
        doReturn(false).when(context).producesDeclared(SubmoduleStatement.class);

        final var namespaceBinding = context.newNamespaceBinding();
        assertSame(FOO_NS, namespaceBinding.lookupModule(FOO));
        assertSame(FOO_NS, namespaceBinding.lookupModule(FOO));
        verify(context).namespace(any());
        verify(context).namespaceItem(any(), any());
    }

    @Test
    void lookupModuleBelongsTo() {
        doReturn(Map.of()).when(context).namespace(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX);
        doReturn(true).when(context).producesDeclared(SubmoduleStatement.class);
        doReturn(Map.of("foo", FOO)).when(context).namespace(ParserNamespaces.BELONGSTO_PREFIX_TO_MODULE_NAME);
        doReturn(FOO_NS).when(context).namespaceItem(ParserNamespaces.MODULE_NAME_TO_QNAME, FOO);

        final var namespaceBinding = context.newNamespaceBinding();
        assertSame(FOO_NS, namespaceBinding.lookupModule(FOO));
        assertSame(FOO_NS, namespaceBinding.lookupModule(FOO));
        verify(context, times(2)).namespace(any());
        verify(context).namespaceItem(any(), any());
    }

    @Test
    void lookupModuleNotFound() {
        doReturn(Map.of()).when(context).namespace(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX);
        doReturn(false).when(context).producesDeclared(SubmoduleStatement.class);

        final var namespaceBinding = context.newNamespaceBinding();
        assertNull(namespaceBinding.lookupModule(FOO));
    }
}
