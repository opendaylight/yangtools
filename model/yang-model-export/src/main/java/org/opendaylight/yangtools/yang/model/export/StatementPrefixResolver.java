/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RootEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;

/**
 * Utility resolver to disambiguate imports.
 */
final class StatementPrefixResolver {
    private static final class Conflict {
        private final Collection<Entry<DeclaredStatement<?>, String>> statements;

        Conflict(final Collection<Entry<DeclaredStatement<?>, String>> entries) {
            statements = requireNonNull(entries);
        }

        @Nullable String findPrefix(final DeclaredStatement<?> stmt) {
            return statements.stream().filter(entry -> contains(entry.getKey(), stmt)).findFirst().map(Entry::getValue)
                    .orElse(null);
        }

        private static boolean contains(final DeclaredStatement<?> haystack, final DeclaredStatement<?> needle) {
            if (haystack == needle) {
                return true;
            }
            for (var child : haystack.declaredSubstatements()) {
                if (contains(child, needle)) {
                    return true;
                }
            }
            return false;
        }
    }

    private final Map<QNameModule, ?> lookup;

    private StatementPrefixResolver(final ImmutableMap<QNameModule, ?> map) {
        lookup = requireNonNull(map);
    }

    private StatementPrefixResolver(final RootEffectiveStatement<?> stmt) {
        lookup = ImmutableMap.copyOf(stmt.namespacePrefixes());
    }

    static StatementPrefixResolver forModule(final ModuleEffectiveStatement module) {
        final var submodules = module.submodules();
        if (submodules.isEmpty()) {
            // Simple: it's just the module
            return new StatementPrefixResolver(module);
        }

        // Stage one: check what everyone thinks about imports
        final var prefixToNamespaces = new HashMap<String, Multimap<QNameModule, EffectiveStatement<?, ?>>>();
        indexPrefixes(prefixToNamespaces, module);
        for (var submodule : submodules) {
            indexPrefixes(prefixToNamespaces, submodule);
        }

        // Stage two: see what QNameModule -> prefix mappings there are. We will need to understand this in step three
        final var namespaceToPrefixes = HashMultimap.<QNameModule, String>create();
        for (var entry : prefixToNamespaces.entrySet()) {
            for (var namespace : entry.getValue().keySet()) {
                namespaceToPrefixes.put(namespace, entry.getKey());
            }
        }

        // Stage three: resolve first order of conflicts, potentially completely resolving mappings...
        final var builder = ImmutableMap.<QNameModule, Object>builderWithExpectedSize(prefixToNamespaces.size());

        // ... first resolve unambiguous mappings ...
        final var it = prefixToNamespaces.entrySet().iterator();
        while (it.hasNext()) {
            final var entry = it.next();
            final var modules = entry.getValue();
            if (modules.size() == 1) {
                // Careful now: the namespace needs to be unambiguous
                final var namespace = modules.keys().iterator().next();
                if (namespaceToPrefixes.get(namespace).size() == 1) {
                    builder.put(namespace, entry.getKey());
                    it.remove();
                }
            }
        }

        // .. check for any remaining conflicts ...
        if (!prefixToNamespaces.isEmpty()) {
            final var conflicts = ArrayListMultimap.<QNameModule, Entry<DeclaredStatement<?>, String>>create();
            for (var entry : prefixToNamespaces.entrySet()) {
                for (var namespaceEntry : entry.getValue().entries()) {
                    conflicts.put(namespaceEntry.getKey(),
                        new SimpleImmutableEntry<>(namespaceEntry.getValue().getDeclared(), entry.getKey()));
                }
            }

            builder.putAll(Maps.transformValues(conflicts.asMap(), Conflict::new));
        }

        return new StatementPrefixResolver(builder.build());
    }

    static StatementPrefixResolver forSubmodule(final SubmoduleEffectiveStatement submodule) {
        return new StatementPrefixResolver(submodule);
    }

    Optional<String> findPrefix(final DeclaredStatement<?> stmt) {
        final var module = stmt.statementDefinition().getStatementName().getModule();
        if (YangConstants.RFC6020_YIN_MODULE.equals(module)) {
            return Optional.empty();
        }

        final var obj = lookup.get(module);
        if (obj != null) {
            return decodeEntry(obj, stmt);
        }
        if (module.revision() != null) {
            throw new IllegalArgumentException("Failed to find prefix for statement " + stmt);
        }

        // FIXME: this is an artifact of commonly-bound statements in parser, which means a statement's name
        //        does not have a Revision. We'll need to find a solution to this which is acceptable. There
        //        are multiple ways of fixing this:
        //        - perhaps EffectiveModuleStatement should be giving us a statement-to-EffectiveModule map?
        //        - or DeclaredStatement should provide the prefix?
        //        The second one seems cleaner, as that means we would not have perform any lookup at all...
        Entry<QNameModule, ?> match = null;
        for (var entry : lookup.entrySet()) {
            final var ns = entry.getKey();
            if (module.equals(ns.withoutRevision())
                && (match == null || Revision.compare(match.getKey().revision(), ns.revision()) < 0)) {
                match = entry;
            }
        }

        return match == null ? null : decodeEntry(match.getValue(), stmt);
    }

    private static Optional<String> decodeEntry(final Object entry, final DeclaredStatement<?> stmt) {
        if (entry instanceof String str) {
            return Optional.of(str);
        } else if (entry instanceof Conflict conflict) {
            final var prefix = conflict.findPrefix(stmt);
            checkArgument(prefix != null, "Failed to find prefix for statement %s", stmt);
            verify(!prefix.isEmpty(), "Empty prefix for statement %s", stmt);
            return Optional.of(prefix);
        } else {
            throw new VerifyException("Unexpected entry " + entry);
        }
    }

    private static void indexPrefixes(final Map<String, Multimap<QNameModule, EffectiveStatement<?, ?>>> map,
            final RootEffectiveStatement<?> stmt) {
        for (var entry : stmt.namespacePrefixes()) {
            map.computeIfAbsent(entry.getValue(), key -> ArrayListMultimap.create()).put(entry.getKey(), stmt);
        }
    }
}
