/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QNameModule;

@ExtendWith(MockitoExtension.class)
class StmtContextNamespaceBindingTest {
    private static final @NonNull QNameModule FOO_NS = QNameModule.ofRevision("foo", "2025-12-27");

    @Mock
    private RootStatementContext<?, ?, ?> context;

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
}
