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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriterExtension;

/**
 * A {@link NormalizedNodeStreamWriterExtension} exposed by stream writers which can handle mount point data, notably
 * providing the facilities to resolve a mount point schema and normalize mount point contents into a normalized
 * structure.
 */
@Beta
@NonNullByDefault
public interface StreamWriterMountPointExtension extends NormalizedNodeStreamWriterExtension {
    /**
     * Start a new mount point with a specific mount point context. The returned writer will be used to emit the content
     * of the mount point, without touching the writer to which this extension is attached to. Once that is done, the
     * returned writer will be {@link NormalizedNodeStreamWriter#close()}d, at which point the parent writer will be
     * used again to emit the rest of the tree.
     *
     * @param mountId Mount point identifier
     * @param mountCtx Mount point context
     * @return A new NormalizedNodeStreamWriter
     * @throws IOException if an error occurs
     */
    NormalizedNodeStreamWriter startMountPoint(MountPointIdentifier mountId, MountPointContext mountCtx)
            throws IOException;
}
