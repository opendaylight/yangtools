/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentBindingException;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

@ExtendWith(MockitoExtension.class)
abstract class AbstractNamespaceBindingTest {
    static final @NonNull QNameModule FOO = QNameModule.ofRevision("foons", "2025-12-16");
    static final @NonNull QNameModule BAR = QNameModule.ofRevision("barns", "2025-12-16");

    @Mock
    NamespaceBinding namespaceBinding;

    CommonArgumentParsers parsers;

    @BeforeEach
    final void beforeEach() {
        parsers = new CommonArgumentParsers(namespaceBinding);
    }

    @Test
    abstract void happyParseArgument() throws Exception;

    static final ArgumentBindingException assertBindingException(final Executable executable) {
        return assertThrowsExactly(ArgumentBindingException.class, executable);
    }

    static final ArgumentSyntaxException assertSyntaxException(final Executable executable) {
        return assertThrowsExactly(ArgumentSyntaxException.class, executable);
    }
}
