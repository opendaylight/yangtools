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
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Common interface capturing general layout of a top-level YANG declared statement -- either
 * a {@link ModuleEffectiveStatement} or a {@link SubmoduleEffectiveStatement}.
 */
@Beta
public sealed interface RootEffectiveStatement<D extends RootDeclaredStatement>
        extends EffectiveStatement<Unqualified, D> permits ModuleEffectiveStatement, SubmoduleEffectiveStatement {
    /**
     * Find the {@link ModuleEffectiveStatement} statement based on {@link PrefixEffectiveStatement}s, be it direct
     * substatement or a substatement of a {@link ImportEffectiveStatement} substatement.
     *
     * @return Imported {@link ModuleEffectiveStatement}, or absent
     */
    @NonNull Optional<ModuleEffectiveStatement> findReachableModule(@NonNull String prefix);

    /**
     * Enumerate all modules reachable from this module. This is recursive relationship: every
     * {@link RootEffectiveStatement} is considered reachable from itself under its local prefix. Returned collection
     * is guaranteed not to contain more than one element with the same {@link ReachableModule#prefix}.
     *
     * @return All imported modu
     */
    Collection<Entry<String, ModuleEffectiveStatement>> reachableModules();

    /**
     * Namespace mapping all known {@link QNameModule}s to their encoding prefixes. This includes the declaration
     * from prefix/namespace/revision and all imports as they were resolved.
     */
    @NonNull Optional<String> findNamespacePrefix(@NonNull QNameModule namespace);

    Collection<Entry<QNameModule, String>> importedNamespaces();
}
