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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentBindingException;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentParser;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

@ExtendWith(MockitoExtension.class)
abstract class AbstractNamespaceBindingTest<A> {
    static final @NonNull QNameModule FOO = QNameModule.ofRevision("foons", "2025-12-16");
    static final @NonNull QNameModule BAR = QNameModule.ofRevision("barns", "2025-12-16");
    static final @NonNull Unqualified ABC = Unqualified.of("abc");

    @Mock
    NamespaceBinding namespaceBinding;

    abstract ArgumentParser<@NonNull A> parser();

    final ArgumentBindingException assertBindingException(final String str) {
        return assertThrowsExactly(ArgumentBindingException.class, () -> parser().parseArgument(str));
    }

    final ArgumentSyntaxException assertSyntaxException(final String str) {
        return assertThrowsExactly(ArgumentSyntaxException.class, () -> parser().parseArgument(str));
    }
}
