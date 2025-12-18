/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

@ExtendWith(MockitoExtension.class)
class StmtContextNamespaceBindingTest {
    private static final @NonNull QNameModule FOO_NS = QNameModule.ofRevision("foo", "2025-12-27");
    private static final @NonNull Unqualified FOO = Unqualified.of("foo");

    @Mock
    private RootStmtContext<?, ?, ?> context;
    @Mock
    private StmtContext<?, ?, ?> importedContext;

    private StmtContextNamespaceBinding namespaceBinding;

    @BeforeEach
    void beforeEach() {
        namespaceBinding = new StmtContextNamespaceBinding(context);
    }

    @Test
    void currentModuleInvokesDefiningModule() {
        doReturn(FOO_NS).when(context).definingModule();
        assertSame(FOO_NS, namespaceBinding.currentModule());
        assertSame(FOO_NS, namespaceBinding.currentModule());
        verify(context).definingModule();
    }

    @Test
    void lookupModuleImported() {
        doReturn(importedContext).when(context).namespaceItem(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX, "foo");
        doReturn(FOO_NS).when(context).namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, importedContext);
        assertSame(FOO_NS, namespaceBinding.lookupModule(FOO));
        assertSame(FOO_NS, namespaceBinding.lookupModule(FOO));
        verify(context, times(2)).namespaceItem(any(), any());
    }

    @Test
    void lookupModuleBelongsTo() {
        doReturn(null).when(context).namespaceItem(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX, "foo");
        doReturn(null).when(context).namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, null);
        doReturn(true).when(context).producesDeclared(SubmoduleStatement.class);
        doReturn(FOO).when(context).namespaceItem(ParserNamespaces.BELONGSTO_PREFIX_TO_MODULE_NAME, "foo");
        doReturn(FOO_NS).when(context).namespaceItem(ParserNamespaces.MODULE_NAME_TO_QNAME, FOO);
        assertSame(FOO_NS, namespaceBinding.lookupModule(FOO));
        assertSame(FOO_NS, namespaceBinding.lookupModule(FOO));
        verify(context, times(4)).namespaceItem(any(), any());
    }

    @Test
    void lookupModuleNotFound() {
        doReturn(null).when(context).namespaceItem(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX, "foo");
        doReturn(null).when(context).namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, null);
        doReturn(false).when(context).producesDeclared(SubmoduleStatement.class);
        assertNull(namespaceBinding.lookupModule(FOO));
    }

    @Test
    void toStringChanges() {
        doReturn("xyzzy").when(context).toString();
        assertEquals("StmtContextNamespaceBinding{context=xyzzy, cached=0}", namespaceBinding.toString());

        currentModuleInvokesDefiningModule();
        assertEquals("StmtContextNamespaceBinding{context=xyzzy, cached=1}", namespaceBinding.toString());

        lookupModuleImported();
        assertEquals("StmtContextNamespaceBinding{context=xyzzy, cached=2}", namespaceBinding.toString());
    }
}
