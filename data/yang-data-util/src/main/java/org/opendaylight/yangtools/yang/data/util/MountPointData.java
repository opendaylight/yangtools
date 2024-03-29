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
import org.opendaylight.yangtools.yang.common.MountPointLabel;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointChild;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContextFactory;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContextFactory.ContainerName;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointException;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MountPointExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YANG Schema Mount-supported data attached to either a {@code list} item or a {@code container}.
 */
@Beta
public final class MountPointData {
    private static final Logger LOG = LoggerFactory.getLogger(MountPointData.class);

    private final Map<ContainerName, MountPointChild> yangLib = new EnumMap<>(ContainerName.class);
    private final List<MountPointChild> children = new ArrayList<>();
    private final MountPointContextFactory contextFactory;
    private final @NonNull MountPointLabel label;

    private MountPointChild schemaMounts;

    MountPointData(final MountPointLabel label, final MountPointContextFactory contextFactory) {
        this.label = requireNonNull(label);
        this.contextFactory = requireNonNull(contextFactory);
    }

    public @NonNull MountPointLabel label() {
        return label;
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
        final var mountWriter = writer.extension(MountPointExtension.class);
        if (mountWriter == null) {
            LOG.debug("Writer {} does not support mount points, ignoring data in {}", writer, label);
            return;
        }

        final MountPointContext mountCtx;
        try {
            mountCtx = contextFactory.createContext(yangLib, schemaMounts);
        } catch (MountPointException e) {
            throw new IOException("Failed to resolve mount point " + label, e);
        }
        try (NormalizedNodeStreamWriter nestedWriter = mountWriter.startMountPoint(label, mountCtx)) {
            for (MountPointChild child : children) {
                child.writeTo(nestedWriter, mountCtx);
            }
        }
    }
}
