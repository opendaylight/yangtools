/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractSimpleIdentifiable;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointChild;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContext;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.rfc8528.data.api.StreamWriterMountPointExtension;
import org.opendaylight.yangtools.rfc8528.data.api.YangLibraryConstants.ContainerName;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YANG Schema Mount-supported data attached to either a {@code list} item or a {@code container}.
 */
@Beta
public final class MountPointData extends AbstractSimpleIdentifiable<MountPointIdentifier> {
    private static final Logger LOG = LoggerFactory.getLogger(MountPointData.class);

    private final Map<ContainerName, MountPointChild> yangLib = new EnumMap<>(ContainerName.class);
    private final List<MountPointChild> children = new ArrayList<>();
    private final MountPointContextFactory contextFactory;

    private MountPointChild schemaMounts;

    MountPointData(final MountPointIdentifier mountId, final MountPointContextFactory contextFactory) {
        super(mountId);
        this.contextFactory = requireNonNull(contextFactory);
    }

    public void setContainer(final @NonNull ContainerName containerName, final @NonNull MountPointChild data) {
        final MountPointChild prev = yangLib.putIfAbsent(containerName, requireNonNull(data));
        checkState(prev == null, "Attempted to duplicate container %s data %s with %s", containerName, prev, data);
        addChild(data);
    }

    public void setSchemaMounts(final @NonNull MountPointChild data) {
        checkState(schemaMounts == null, "Attempted to reset schema-mounts from %s to %s", schemaMounts, data);
        schemaMounts = requireNonNull(data);
        addChild(data);
    }

    public void addChild(final @NonNull MountPointChild data) {
        children.add(requireNonNull(data));
    }

    void write(final @NonNull NormalizedNodeStreamWriter writer) throws IOException {
        final StreamWriterMountPointExtension mountWriter = writer.getExtensions()
            .getInstance(StreamWriterMountPointExtension.class);
        if (mountWriter == null) {
            LOG.debug("Writer {} does not support mount points, ignoring data in {}", writer, getIdentifier());
            return;
        }

        final MountPointContext mountCtx;
        try {
            mountCtx = contextFactory.createContext(yangLib, schemaMounts);
        } catch (YangParserException e) {
            throw new IOException("Failed to resolve mount point " + getIdentifier(), e);
        }
        try (NormalizedNodeStreamWriter nestedWriter = mountWriter.startMountPoint(getIdentifier(), mountCtx)) {
            for (MountPointChild child : children) {
                child.writeTo(nestedWriter, mountCtx);
            }
        }
    }
}
