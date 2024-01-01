/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Optional;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.MountPointLabel;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContextFactory;
import org.opendaylight.yangtools.yang.data.util.AbstractMountPointContextFactory.MountPointDefinition;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

record ImmutableMountPointContext(
        @NonNull EffectiveModelContext modelContext,
        @NonNull ImmutableMap<MountPointLabel, MountPointDefinition> mountPoints,
        @NonNull Function<MountPointDefinition, MountPointContextFactory> createFactory)
        implements Immutable, MountPointContext {
    ImmutableMountPointContext {
        requireNonNull(modelContext);
        requireNonNull(mountPoints);
        requireNonNull(createFactory);
    }

    ImmutableMountPointContext(final EffectiveModelContext modelContext,
            final Iterable<MountPointDefinition> mountPoints,
            final Function<MountPointDefinition, MountPointContextFactory> createFactory) {
        this(modelContext, Maps.uniqueIndex(mountPoints, MountPointDefinition::label), createFactory);
    }

    @Override
    public Optional<MountPointContextFactory> findMountPoint(final MountPointLabel label) {
        final var def = mountPoints.get(requireNonNull(label));
        return def == null ? Optional.empty() : Optional.of(createFactory.apply(def));
    }
}
