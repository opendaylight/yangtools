/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.api;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchema;
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
     * Start a new mount point with a specific root context.
     *
     * @param mountSchema Mount point schema
     * @return A new NormalizedNodeStreamWriter
     * @throws NullPointerException if mountSchema is null
     * @throws IOException if an error occurs
     */
    @NonNull Optional<NormalizedNodeStreamWriter> startMountPoint(@NonNull MountPointSchema mountSchema)
            throws IOException;
}
