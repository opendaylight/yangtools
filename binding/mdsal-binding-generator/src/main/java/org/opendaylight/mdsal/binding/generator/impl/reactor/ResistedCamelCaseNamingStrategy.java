/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An intermediate strategy which passed class name without modification. If falls back to
 * {@link CamelCaseWithNamespaceNamingStrategy}.
 */
@NonNullByDefault
final class ResistedCamelCaseNamingStrategy extends FallbackCamelCaseNamingStrategy {
    ResistedCamelCaseNamingStrategy(final CamelCaseNamingStrategy delegate) {
        super(delegate);
    }

    @Override
    String simpleClassName() {
        return delegate().simpleClassName();
    }

    @Override
    @NonNull ClassNamingStrategy fallback() {
        return new CamelCaseWithNamespaceNamingStrategy(delegate());
    }
}
