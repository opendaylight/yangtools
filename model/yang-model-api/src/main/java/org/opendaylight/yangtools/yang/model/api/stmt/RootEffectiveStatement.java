/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Common interface capturing general layout of a top-level YANG declared statement -- either
 * a {@link ModuleEffectiveStatement} or a {@link SubmoduleEffectiveStatement}.
 *
 * <p>
 * Both these statements have a relationship to lexical and semantic interpretation of a particular YANG (or YIN) file.
 * The core principle is that every XML prefix is bound to a particular {@link ModuleEffectiveStatement}, exposed via
 * {@link #findReachableModule(String)} and {@link #reachableModules()}. The secondary effect of it is that each known
 * {@link QNameModule} is known under a (preferred) prefix, exposed via {@link #findNamespacePrefix(QNameModule)}.
 */
@Beta
public sealed interface RootEffectiveStatement<D extends RootDeclaredStatement>
        extends EffectiveStatement<Unqualified, D> permits ModuleEffectiveStatement, SubmoduleEffectiveStatement {
    /**
     * Find the {@link ModuleEffectiveStatement} statement based on {@link PrefixEffectiveStatement}s, be it direct
     * substatement or a substatement of a {@link ImportEffectiveStatement} substatement.
     *
     * @return prefix Imported {@link ModuleEffectiveStatement}, or absent
     * @throws NullPointerException if {@code prefix} is {@code null}
     */
    @NonNull Optional<ModuleEffectiveStatement> findReachableModule(@NonNull String prefix);

    /**
     * Enumerate all modules reachable from this module. This is recursive relationship: every
     * {@link RootEffectiveStatement} is considered reachable from itself under its local prefix. Returned collection
     * is guaranteed not to contain more than one element with the same {@link Entry#getKey()}.
     *
     * @return All {@link ModuleEffectiveStatement}s reachable in this {@code module} or {@code submodule}, coupled with
     *         their preferred prefix.
     */
    @NonNull Collection<Entry<String, ModuleEffectiveStatement>> reachableModules();

    /**
     * Find the preferred prefix to use with a particular namespace.
     *
     * @param namespace A bound namespace, represented as {@link QNameModule}
     * @return Preferred prefix, or empty
     * @throws NullPointerException if {@code namespace} is {@code null}
     */
    @NonNull Optional<String> findNamespacePrefix(@NonNull QNameModule namespace);

    /**
     * Enumeration of all namespace-to-prefix mappings. This generally corresponds to a {@link Map#entrySet()}, but we
     * do not want to be bogged down by a {@link Set}.
     */
    Collection<Entry<QNameModule, String>> namespacePrefixes();
}
