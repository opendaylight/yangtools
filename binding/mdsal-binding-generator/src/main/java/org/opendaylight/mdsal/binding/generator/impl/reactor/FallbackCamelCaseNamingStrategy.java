/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A fallback {@link ClassNamingStrategy} derived from a {@link CamelCaseNamingStrategy}.
 */
@NonNullByDefault
abstract class FallbackCamelCaseNamingStrategy extends ClassNamingStrategy {
    private final CamelCaseNamingStrategy delegate;

    FallbackCamelCaseNamingStrategy(final CamelCaseNamingStrategy delegate) {
        this.delegate = requireNonNull(delegate);
    }

    final CamelCaseNamingStrategy delegate() {
        return delegate;
    }

    @Override
    final String rootName() {
        return delegate.rootName();
    }

    @Override
    final String childPackage() {
        return delegate.childPackage();
    }

    @Override
    final ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("delegate", delegate);
    }
}
