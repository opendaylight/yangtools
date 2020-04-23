/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rcf8528.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Optional;
import java.util.function.Function;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.rcf8528.data.util.AbstractMountPointContextFactory.MountPointDefinition;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContext;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.AbstractEffectiveModelContextProvider;

final class ImmutableMountPointContext extends AbstractEffectiveModelContextProvider
        implements Immutable, MountPointContext {
    private final ImmutableMap<MountPointIdentifier, MountPointDefinition> mountPoints;
    private final Function<MountPointDefinition, MountPointContextFactory> createFactory;

    ImmutableMountPointContext(final EffectiveModelContext modelContext,
            final Iterable<MountPointDefinition> mountPoints,
            final Function<MountPointDefinition, MountPointContextFactory> createFactory) {
        super(modelContext);
        this.mountPoints = Maps.uniqueIndex(mountPoints, MountPointDefinition::getIdentifier);
        this.createFactory = requireNonNull(createFactory);
    }

    @Override
    public Optional<MountPointContextFactory> findMountPoint(final MountPointIdentifier label) {
        final MountPointDefinition def = mountPoints.get(requireNonNull(label));
        return def == null ? Optional.empty() : Optional.of(createFactory.apply(def));
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("mountPoints", mountPoints);
    }
}
