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
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchemaResolver;

/**
 * Main entry point for parsing data behind a dynamic mount point. It can hand out resolvers, which can optionally
 * take further data to assemble a final SchemaContext to interpret data.
 */
@Beta
public interface MountPointSchemaSupport {
    /**
     * Attempt to acquire a {@link MountPointSchemaResolver} to resolve schemas for the purposes of interpreting this
     * mount point. An empty result indicates the mount point is not attached.
     *
     * @param label Mount point label, as defined via the use of {@code mount-point} statement
     * @return An optional handler for mount point data
     * @throws NullPointerException if label is null
     */
    Optional<MountPointSchemaResolver> findMountPoint(@NonNull MountPointIdentifier label);
}
