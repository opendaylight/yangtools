/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.api;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * A single <a href="">RFC8525</a> {@code module} or {@code import-only-module} list entry.
 */
public record YangLibModule(@NonNull SourceIdentifier identifier, @NonNull XMLNamespace namespace,
        @NonNull ImmutableMap<Unqualified, YangLibSubmodule> submodules, @NonNull ImmutableSet<Unqualified> features,
        @NonNull ImmutableSet<Unqualified> deviationModuleNames, @NonNull SchemaSourceRepresentation source) {
    public YangLibModule {
        requireNonNull(identifier);
        requireNonNull(namespace);
        requireNonNull(submodules);
        requireNonNull(features);
        requireNonNull(deviationModuleNames);
    }
}
