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
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;

/**
 * A single <a href="https://www.rfc-editor.org/rfc/rfc8525">RFC8525</a> {@code module} or {@code import-only-module}
 * list entry. Note that the YANG definition has two dissimilar instances, but that really is an artifact of how indexes
 * work in YANG.
 *
 * @param identifier {@link SourceIdentifier} of this module, e.g. the combination of {@code name} and {@code revision}
 * @param namespace {@link XMLNamespace} of this module
 * @param submodules Submodules of this module
 * @param features The set of supported features in this module
 * @param deviationModuleNames Names of modules containing {@code deviate} statements targetting this module
 * @param source A {@link SourceRepresentation} of the module
 */
public record YangLibModule(@NonNull SourceIdentifier identifier, @NonNull XMLNamespace namespace,
        @NonNull ImmutableMap<Unqualified, YangLibSubmodule> submodules, @NonNull ImmutableSet<Unqualified> features,
        @NonNull ImmutableSet<Unqualified> deviationModuleNames, @NonNull SourceRepresentation source) {
    public YangLibModule {
        requireNonNull(identifier);
        requireNonNull(namespace);
        requireNonNull(submodules);
        requireNonNull(features);
        requireNonNull(deviationModuleNames);
    }
}
