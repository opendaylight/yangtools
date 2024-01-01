/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;

/**
 * A {@link SchemaSourceInfo} about a {@code submodule}.
 */
@NonNullByDefault
public record SubmoduleSourceInfo(
        Unqualified name,
        YangVersion yangVersion,
        Unqualified belongsTo,
        ImmutableSet<Revision> revisions,
        ImmutableSet<Import> imports,
        ImmutableSet<Include> includes) implements SchemaSourceInfo {
    public SubmoduleSourceInfo {
        requireNonNull(name);
        requireNonNull(yangVersion);
        requireNonNull(belongsTo);
        requireNonNull(revisions);
        requireNonNull(imports);
        requireNonNull(includes);
    }
}
