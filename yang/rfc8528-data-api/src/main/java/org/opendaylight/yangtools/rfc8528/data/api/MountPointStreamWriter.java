/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointMetadata;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointMetadataResolver;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriterExtension;

/**
 * A {@link NormalizedNodeStreamWriterExtension} exposed by stream writers which can handle mount point data, notably
 * providing the facilities to resolve a mount point schema and normalize mount point contents into a normalized
 * structure.
 */
@Beta
public interface MountPointStreamWriter extends NormalizedNodeStreamWriterExtension {
    /**
     * Attempt to acquire a {@link MountPointMetadataResolver} to resolve schemas for the purposes of interpreting this
     * mount point. An empty result indicates the mount point is not attached.
     *
     * @param label Mount point label, as defined via the use of {@code mount-point} statement
     * @return An optional handler for mount point data
     * @throws NullPointerException if label is null
     */
    Optional<MountPointMetadataResolver> findMountPoint(@NonNull MountPointIdentifier label);

    /**
     * Start a new mount point with a specific root context.
     *
     * @param label Mount point label
     * @param mountContext SchemaContext associated with the context
     * @return A new NormalizedNodeStreamWriter
     */
    @NonNull NormalizedNodeStreamWriter startMountPoint(@NonNull MountPointMetadata metadata);
}
