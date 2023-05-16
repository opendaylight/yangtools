/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointLabel;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMountPoint;

@NonNullByDefault
public record ImmutableNormalizedMountPoint(
        MountPointLabel label,
        MountPointContext context,
        ImmutableMap<PathArgument, DataContainerChild> children) implements NormalizedMountPoint {
    public ImmutableNormalizedMountPoint {
        requireNonNull(label);
        requireNonNull(context);
        requireNonNull(children);
    }
}
