/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

/**
 * Interface for resolving XML prefixes to their bound {@link QNameModule}s. This resolution entails determining
 * the correct {@link Revision} bound at the use site.
 */
@NonNullByDefault
public final class PrefixResolver implements Immutable {
    private final Map<String, QNameModule> prefixToModule;

    private PrefixResolver(final Map<String, QNameModule> prefixToModule) {
        this.prefixToModule = requireNonNull(prefixToModule);
    }

    public static PrefixResolver of(final Map<Unqualified, QNameModule> prefixToModule) {
        return new PrefixResolver(prefixToModule.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(entry -> entry.getKey().getLocalName(), Map.Entry::getValue)));
    }

    /**
     * Returns QNameModule (namespace + revision) associated with supplied prefix.
     *
     * @param prefix Prefix
     * @return QNameModule associated with supplied prefix, or null if prefix is not defined.
     */
    @Nullable QNameModule resolvePrefix(final String prefix) {
        return prefixToModule.get(prefix);
    }

    @Override
    public String toString() {
        // FIXME: improve by doing a smart MMap.toString()-like thing, but also order by String.compareTo()
        return MoreObjects.toStringHelper(this).add("prefixToModule", prefixToModule).toString();
    }
}
