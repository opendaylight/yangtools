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
     * Namespace mapping all known prefixes in a module to their modules. Note this namespace includes the module
     * in which it is instantiated.
     */
    @NonNull Optional<ModuleEffectiveStatement> findImportedModule(@NonNull String prefix);

    Collection<Entry<String, ModuleEffectiveStatement>> importedModules();

    /**
     * Namespace mapping all known {@link QNameModule}s to their encoding prefixes. This includes the declaration
     * from prefix/namespace/revision and all imports as they were resolved.
     */
    @NonNull Optional<String> findNamespacePrefix(@NonNull QNameModule namespace);

    Collection<Entry<QNameModule, String>> importedNamespaces();
}
