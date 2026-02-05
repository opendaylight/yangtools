/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixResolver;

/**
 * An immutable {@link PrefixResolver}.
 *
 * @since 15.0.0
 */
@NonNullByDefault
final class ImmutablePrefixResolver implements Immutable, PrefixResolver {
    private final Map<String, QNameModule> prefixToModule;

    private ImmutablePrefixResolver(final Map<String, QNameModule> prefixToModule) {
        this.prefixToModule = requireNonNull(prefixToModule);
    }

    static PrefixResolver of(final Map<Unqualified, QNameModule> prefixToModule) {
        return new ImmutablePrefixResolver(prefixToModule.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(entry -> entry.getKey().getLocalName(), Map.Entry::getValue)));
    }

    @Override
    public @Nullable QNameModule resolvePrefix(final String prefix) {
        return prefixToModule.get(prefix);
    }

    @Override
    public String toString() {
        // FIXME: improve by doing a smart MMap.toString()-like thing, but also order by String.compareTo()
        return MoreObjects.toStringHelper(this).add("prefixToModule", prefixToModule).toString();
    }
}
