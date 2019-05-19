/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement.NameToEffectiveSubmoduleNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement.QNameModuleToPrefixNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;

/**
 * Utility resolver to disambiguate imports
 *
 * @author Robert Varga
 *
 */
final class StatementPrefixResolver {
    private static final class Conflict {
        private final Collection<Entry<DeclaredStatement<?>, String>> statements;

        Conflict(final Collection<Entry<DeclaredStatement<?>, String>> entries) {
            this.statements = requireNonNull(entries);
        }

        Optional<String> findPrefix(final DeclaredStatement<?> stmt) {
            return statements.stream().filter(entry -> contains(entry.getKey(), stmt)).findFirst().map(Entry::getValue);
        }

        private static boolean contains(final DeclaredStatement<?> haystack, final DeclaredStatement<?> needle) {
            if (haystack == needle) {
                return true;
            }
            for (DeclaredStatement<?> child : haystack.declaredSubstatements()) {
                if (contains(child, needle)) {
                    return true;
                }
            }
            return false;
        }
    }

    private final Map<QNameModule, ?> lookup;

    private StatementPrefixResolver(final Map<QNameModule, String> map) {
        this.lookup = ImmutableMap.copyOf(map);
    }

    private StatementPrefixResolver(final ImmutableMap<QNameModule, ?> map) {
        this.lookup = requireNonNull(map);
    }

    static StatementPrefixResolver forModule(final ModuleEffectiveStatement module) {
        final Map<QNameModule, String> imports = module.findAll(QNameModuleToPrefixNamespace.class);
        final Collection<SubmoduleEffectiveStatement> submodules = module.findAll(
            NameToEffectiveSubmoduleNamespace.class).values();
        if (submodules.isEmpty()) {
            // Simple: it's just the module
            return new StatementPrefixResolver(imports);
        }

        // Stage one: check what everyone thinks about imports
        final Map<String, Multimap<QNameModule, EffectiveStatement<?, ?>>> prefixToNamespaces = new HashMap<>();
        indexPrefixes(prefixToNamespaces, imports, module);
        for (SubmoduleEffectiveStatement submodule : submodules) {
            // FIXME: this does not work ... index substatements?
            indexPrefixes(prefixToNamespaces, submodule.findAll(QNameModuleToPrefixNamespace.class), submodule);
        }

        // Stage two: resolve first order of conflicts, potentially completely resolving mappings...
        final Builder<QNameModule, Object> builder = ImmutableMap.builderWithExpectedSize(prefixToNamespaces.size());

        // ... first resolve unambiguous mappings ...
        final Iterator<Entry<String, Multimap<QNameModule, EffectiveStatement<?, ?>>>> it =
                prefixToNamespaces.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<String, Multimap<QNameModule, EffectiveStatement<?, ?>>> entry = it.next();
            final Multimap<QNameModule, EffectiveStatement<?, ?>> modules = entry.getValue();
            if (modules.size() == 1) {
                builder.put(modules.keys().iterator().next(), entry.getKey());
                it.remove();
            }
        }

        // .. check for any remaining conflicts ...
        if (!prefixToNamespaces.isEmpty()) {
            final Multimap<QNameModule, Entry<DeclaredStatement<?>, String>> conflicts = ArrayListMultimap.create();
            for (Entry<String, Multimap<QNameModule, EffectiveStatement<?, ?>>> entry : prefixToNamespaces.entrySet()) {
                for (Entry<QNameModule, EffectiveStatement<?, ?>> namespace : entry.getValue().entries()) {
                    conflicts.put(namespace.getKey(), new SimpleImmutableEntry<>(namespace.getValue().getDeclared(),
                            entry.getKey()));
                }
            }

            builder.putAll(Maps.transformValues(conflicts.asMap(), Conflict::new));
        }

        return new StatementPrefixResolver(builder.build());
    }

    private static void indexPrefixes(final Map<String, Multimap<QNameModule, EffectiveStatement<?, ?>>> map,
            final Map<QNameModule, String> imports, final EffectiveStatement<?, ?> stmt) {
        for (Entry<QNameModule, String> entry : imports.entrySet()) {
            map.computeIfAbsent(entry.getValue(), key -> ArrayListMultimap.create()).put(entry.getKey(), stmt);
        }
    }

    static StatementPrefixResolver forSubmodule(final SubmoduleEffectiveStatement submodule) {
        // FIXME: this does not work ... index substatements?
        return new StatementPrefixResolver(submodule.findAll(QNameModuleToPrefixNamespace.class));
    }

    @Nullable String findPrefix(final DeclaredStatement<?> stmt) {
        final Object obj = lookup.get(stmt.statementDefinition().getStatementName().getModule());
        if (obj instanceof String) {
            return (String)obj;
        } else if (obj instanceof Conflict) {
            return ((Conflict) obj).findPrefix(stmt).orElse(null);
        } else {
            verify(obj == null);
            return null;
        }
    }
}
