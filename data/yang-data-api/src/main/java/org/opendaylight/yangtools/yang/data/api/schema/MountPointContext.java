/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.MountPointLabel;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * A context of either an explicit (RFC8528 Schema Mount instance) or implicit (system root). It encapsulates a data
 * {@link org.opendaylight.yangtools.yang.model.api.EffectiveModelContext} and information resident in
 * {@code schema-mounts} within this hierarchy.
 *
 * <p>
 * This context exposed enough of an API surface to navigate RFC8528 Schema Mount instaces with respect to normalized,
 * so that proper {@link MountPointLabel}ed {@link NormalizedMountPoint}s can be created. This is enough to integrate
 * with other elements of this API.
 */
public interface MountPointContext {
    /**
     * Return this mount point's {@link EffectiveModelContext}.
     *
     * @return this mount point's {@link EffectiveModelContext}
     */
    @NonNull EffectiveModelContext modelContext();

    /**
     * Attempt to acquire a {@link MountPointContextFactory} to resolve schemas for the purposes of interpreting
     * this mount point. An empty result indicates the mount point is not attached.
     *
     * @param label Mount point label, as defined via the use of {@code mount-point} statement
     * @return An optional handler for mount point data
     * @throws NullPointerException if label is null
     */
    Optional<MountPointContextFactory> findMountPoint(@NonNull MountPointLabel label);

    /**
     * Return an empty {@link MountPointContext} with the specified {@link EffectiveModelContext}.
     *
     * @param modelContext Backing {@link EffectiveModelContext}
     * @return A {@link MountPointContext} containing no {@link MountPointContextFactory}.
     */
    static @NonNull MountPointContext of(final @NonNull EffectiveModelContext modelContext) {
        return new EmptyMountPointContext(modelContext);
    }
}
