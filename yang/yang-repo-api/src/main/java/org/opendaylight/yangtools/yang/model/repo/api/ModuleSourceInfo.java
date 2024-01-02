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
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;

/**
 * A {@link SchemaSourceInfo} about a {@code module}.
 */
@NonNullByDefault
public record ModuleSourceInfo(
        Unqualified name,
        YangVersion yangVersion,
        XMLNamespace namespace,
        String prefix,
        ImmutableSet<Revision> revisions,
        ImmutableSet<Import> imports,
        ImmutableSet<Include> includes) implements SchemaSourceInfo {
    public ModuleSourceInfo {
        requireNonNull(name);
        requireNonNull(yangVersion);
        requireNonNull(namespace);
        requireNonNull(prefix);
        requireNonNull(revisions);
        requireNonNull(imports);
        requireNonNull(includes);
    }
}
