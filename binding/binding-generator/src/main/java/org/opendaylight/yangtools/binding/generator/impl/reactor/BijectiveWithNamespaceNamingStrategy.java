/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;

/**
 * Secondary naming strategy: we use {@link StatementNamespace#suffix()}  on top of
 * BijectiveNamingStrategy.
 */
@NonNullByDefault
final class BijectiveWithNamespaceNamingStrategy extends ForwardingClassNamingStrategy {
    BijectiveWithNamespaceNamingStrategy(final BijectiveNamingStrategy delegate) {
        super(delegate);
    }

    @Override
    String simpleClassName() {
        return delegate().simpleClassName();
    }

    @Override
    @Nullable ClassNamingStrategy fallback() {
        // No fallback
        return null;
    }

    @Override
    @NonNull String rootName() {
        return delegate().rootName();
    }

    @Override
    @NonNull String childPackage() {
        return delegate().childPackage();
    }
}
