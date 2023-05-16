/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointLabel;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * RFC8528 metadata counterpart to a {@link NormalizedNode}. This interface is meant to be used as a companion to
 * a NormalizedNode instance, hence it does not support iterating over its structure like it is possible with
 * {@link NormalizedNode#body()}. Children may be inquired through {@link #getChildren()}.
 */
public interface NormalizedMountpoints extends Identifiable<PathArgument>, Immutable {
    /**
     * Return the set of {@link MountPoint}s defined in this mountpoint node. The map must also be
     * effectively-immutable.
     *
     * @return The set of annotations attached to the corresponding data node.
     */
    @NonNull Map<MountPointLabel, NormalizedMountPoint> getMountPoints();

    /**
     * Returns child nodes. Default implementation returns an empty immutable map.
     *
     * @return Child {@link NormalizedMountpoints}.
     */
    default @NonNull Map<PathArgument, NormalizedMountpoints> getChildren() {
        return ImmutableMap.of();
    }
}
