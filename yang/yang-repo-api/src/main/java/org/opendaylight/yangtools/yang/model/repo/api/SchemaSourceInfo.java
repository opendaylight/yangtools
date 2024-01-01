/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;

/**
 * Linkage information about a particular {@link SchemaSourceRepresentation}. It has two specializations
 * <ol>
 *   <li>{@link ModuleSourceInfo} pertaining to {@link SchemaSourceRepresentation} which have {@code module}
 *       as its root statement</li>
 *   <li>{@link SubmoduleSourceInfo} pertaining to {@link SchemaSourceRepresentation} which have {@code submodule}
 *       as its root statement</li>
 * </ol>
 *
 * <p>
 * This interface captures the basic metadata needed for interpretation and linkage of the source, as represented by the
 * following ABNF constructs placed at the start of a YANG file:
 * <ul>
 *   <li>{@code module-header-stmts} or {@code submodule-header-stmts}</li>
 *   <li>{@code linkage-stmts}</li>
 *   <li>{@code revision-stmts}<li>
 * </ul>
 */
@NonNullByDefault
public sealed interface SchemaSourceInfo permits ModuleSourceInfo, SubmoduleSourceInfo {
    record Import(Unqualified name, String prefix, @Nullable Revision revision) {
        public Import {
            requireNonNull(name);
            requireNonNull(prefix);
        }
    }

    record Include(Unqualified name, @Nullable Revision revision) {
        public Include {
            requireNonNull(name);
        }
    }

    /**
     * The name of this source, as expressed by the argument of {@code module} or {@code submodule} statement.
     *
     * @return name of this source.
     */
    Unqualified name();

    /**
     * {@link YangVersion} of the source. If no {@code yang-version} is present, this method will return
     * {@link YangVersion#VERSION_1}.
     *
     * @return {@link YangVersion} of the source
     */
    YangVersion yangVersion();

    ImmutableList<Revision> revisions();

    ImmutableSet<Import> imports();

    ImmutableSet<Include> includes();
}
