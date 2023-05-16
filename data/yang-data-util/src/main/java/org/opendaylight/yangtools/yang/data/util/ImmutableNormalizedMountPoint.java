/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointLabel;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMetadata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMountPoint;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMountpoints;

/**
 * An immutable {@link NormalizedMountPoint}.
 */
public record ImmutableNormalizedMountPoint(
        @NonNull MountPointLabel label,
        @NonNull MountPointContext context,
        @NonNull ContainerNode data,
        @Nullable NormalizedMetadata metadata,
        @Nullable NormalizedMountpoints mountPoints) implements NormalizedMountPoint {
    public ImmutableNormalizedMountPoint {
        requireNonNull(label);
        requireNonNull(context);
        requireNonNull(data);
    }
}
