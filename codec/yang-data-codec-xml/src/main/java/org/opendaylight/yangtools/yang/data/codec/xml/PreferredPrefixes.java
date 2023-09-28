/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * Prefixes preferred by an {@link EffectiveModelContext}. This acts as an advisory to {@link NamespacePrefixes} for
 * picking namespace prefixes. This works with IETF guidelines, which prefer XML prefix names coming from {@code prefix}
 * statement's argument. This, unfortunately, is not sufficient, as these are not guaranteed to be unique, but we deal
 * with those ambiguities.
 */
abstract sealed class PreferredPrefixes {
    private static final class Precomputed extends PreferredPrefixes implements Immutable {
        static final @NonNull Precomputed EMPTY = new Precomputed(Map.of());

        private final Map<XMLNamespace, String> mappings;

        Precomputed(final Map<XMLNamespace, String> mappings) {
            this.mappings = requireNonNull(mappings);
        }

        @Override
        String prefixForNamespace(final XMLNamespace namespace) {
            return mappings.get(namespace);
        }

        @Override
        Map<XMLNamespace, ?> mappings() {
            return mappings;
        }
    }

    static final class Shared extends PreferredPrefixes implements Mutable {
        private final ConcurrentMap<XMLNamespace, Optional<String>> mappings = new ConcurrentHashMap<>();
        private final EffectiveModelContext modelContext;

        Shared(final EffectiveModelContext modelContext) {
            this.modelContext = requireNonNull(modelContext);
        }

        @Override
        String prefixForNamespace(final XMLNamespace namespace) {
            final var existing = mappings.get(namespace);
            if (existing != null) {
                return existing.orElse(null);
            }

            final var modules = modelContext.findModuleStatements(namespace).iterator();
            // Note: we are not caching anything if we do not find the module
            return modules.hasNext() ? loadPrefix(namespace, modules.next().prefix().argument()) : null;
        }

        /**
         * Completely populate known mappings and return an optimized version equivalent of this object.
         *
         * @return A pre-computed {@link PreferredPrefixes} instance
         */
        @NonNull PreferredPrefixes toPrecomputed() {
            for (var module : modelContext.getModuleStatements().values()) {
                prefixForNamespace(module.namespace().argument());
            }
            return new Precomputed(Map.copyOf(
                Maps.transformValues(Maps.filterValues(mappings, Optional::isPresent), Optional::orElseThrow)));
        }

        @Override
        Map<XMLNamespace, ?> mappings() {
            return mappings;
        }

        private @Nullable String loadPrefix(final XMLNamespace namespace, final String prefix) {
            final var mapping = isValidMapping(namespace, prefix) ? Optional.of(prefix) : Optional.<String>empty();
            final var raced = mappings.putIfAbsent(namespace, mapping);
            return (raced != null ? raced : mapping).orElse(null);
        }

        // Validate that all modules which have the same prefix have also the name namespace
        private boolean isValidMapping(final XMLNamespace namespace, final String prefix) {
            if (startsWithXml(prefix)) {
                return false;
            }
            for (var module : modelContext.getModuleStatements().values()) {
                if (prefix.equals(module.prefix().argument()) && !namespace.equals(module.namespace().argument())) {
                    return false;
                }
            }
            return true;
        }

        // https://www.w3.org/TR/xml-names/#xmlReserved
        private static boolean startsWithXml(final String prefix) {
            if (prefix.length() < 3) {
                return false;
            }
            final var first = prefix.charAt(0);
            if (first != 'x' && first != 'X') {
                return false;
            }
            final var second = prefix.charAt(1);
            if (second != 'm' && second != 'M') {
                return false;
            }
            final var third = prefix.charAt(2);
            return third == 'l' || third == 'L';
        }
    }

    private PreferredPrefixes() {
        // Hidden on purpose
    }

    static @NonNull PreferredPrefixes empty() {
        return Precomputed.EMPTY;
    }

    abstract @Nullable String prefixForNamespace(@NonNull XMLNamespace namespace);

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("mappings", mappings()).toString();
    }

    abstract Map<XMLNamespace, ?> mappings();
}
