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

/**
 * Common interface capturing general layout of a top-level YANG declared statement -- either
 * a {@link ModuleEffectiveStatement} or a {@link SubmoduleEffectiveStatement}.
 */
@Beta
public sealed interface RootEffectiveStatement<D extends RootDeclaredStatement>
        extends ImportEffectiveStatement.MultipleIn<Unqualified, D>,
                ReferenceEffectiveStatement.OptionalIn<Unqualified, D>
        permits ModuleEffectiveStatement, SubmoduleEffectiveStatement {
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
