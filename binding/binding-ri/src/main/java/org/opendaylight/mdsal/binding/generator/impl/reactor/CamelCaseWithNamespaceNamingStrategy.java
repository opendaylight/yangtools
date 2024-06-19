/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNull;

final class CamelCaseWithNamespaceNamingStrategy extends FallbackCamelCaseNamingStrategy {
    CamelCaseWithNamespaceNamingStrategy(final @NonNull CamelCaseNamingStrategy delegate) {
        super(delegate);
    }

    @Override
    String simpleClassName() {
        return delegate().simpleClassName() + delegate().namespace().suffix();
    }

    @Override
    ClassNamingStrategy fallback() {
        // FIXME: MDSAL-502: add a BijectiveNamingStrategy
        //        The algorithm needs to essentially fall back to using escape-based translation scheme, where each
        //        localName results in a unique name, while not conflicting with any possible preferredName. The exact
        //        mechanics for that are TBD. A requirement for that mapping is that it must not rely on definition
        //        order.
        return null;
    }
}
