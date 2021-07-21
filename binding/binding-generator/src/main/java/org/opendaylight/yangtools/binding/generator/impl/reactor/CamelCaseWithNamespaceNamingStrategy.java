/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
final class CamelCaseWithNamespaceNamingStrategy extends FallbackCamelCaseNamingStrategy {
    CamelCaseWithNamespaceNamingStrategy(final @NonNull CamelCaseNamingStrategy delegate) {
        super(delegate);
    }

    @Override
    String simpleClassName() {
        return delegate().simpleClassName() + delegate().namespace().suffix();
    }

    @Override
    @NonNull ClassNamingStrategy fallback() {
        return new BijectiveNamingStrategy(delegate());
    }
}
