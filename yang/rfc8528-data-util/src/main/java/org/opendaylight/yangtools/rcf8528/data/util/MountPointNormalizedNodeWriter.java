/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rcf8528.data.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointNode;
import org.opendaylight.yangtools.rfc8528.data.api.StreamWriterMountPointExtension;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;

/**
 * A {@link MountPointNode}-aware counterpart to {@link NormalizedNodeWriter}. Based on the backing writer's capability
 * it either forwards or filters MountPointNodes.
 */
@Beta
public abstract class MountPointNormalizedNodeWriter extends NormalizedNodeWriter {
    private static final class Filtering extends MountPointNormalizedNodeWriter {
        Filtering(final NormalizedNodeStreamWriter writer) {
            super(writer);
        }

        @Override
        void writeMountPoint(final MountPointNode node) {
            // No-op
        }
    }

    private static final class Forwarding extends MountPointNormalizedNodeWriter {
        private final StreamWriterMountPointExtension mountWriter;

        Forwarding(final NormalizedNodeStreamWriter writer, final StreamWriterMountPointExtension mountWriter) {
            super(writer);
            this.mountWriter = requireNonNull(mountWriter);
        }

        @Override
        void writeMountPoint(final MountPointNode node) throws IOException {
            try (MountPointNormalizedNodeWriter writer = forStreamWriter(mountWriter.startMountPoint(
                    node.getIdentifier(), node.getMountPointContext()))) {
                for (DataContainerChild child : node.body()) {
                    writer.write(child);
                }
            }
        }
    }

    MountPointNormalizedNodeWriter(final NormalizedNodeStreamWriter writer) {
        super(writer);
    }

    public static @NonNull MountPointNormalizedNodeWriter forStreamWriter(final NormalizedNodeStreamWriter writer) {
        final StreamWriterMountPointExtension mountWriter = writer.getExtensions()
            .getInstance(StreamWriterMountPointExtension.class);
        return mountWriter == null ? new Filtering(writer) : new Forwarding(writer, mountWriter);
    }

    public static @NonNull MountPointNormalizedNodeWriter filteringFor(final NormalizedNodeStreamWriter writer) {
        return new Filtering(writer);
    }

    public static @NonNull MountPointNormalizedNodeWriter forwardingFor(final NormalizedNodeStreamWriter writer) {
        final StreamWriterMountPointExtension mountWriter = writer.getExtensions()
            .getInstance(StreamWriterMountPointExtension.class);
        checkArgument(mountWriter != null, "Writer %s does not support mount points", writer);
        return new Forwarding(writer, mountWriter);
    }

    @Override
    protected final boolean wasProcessedAsCompositeNode(final NormalizedNode node) throws IOException {
        if (node instanceof MountPointNode) {
            writeMountPoint((MountPointNode) node);
            return true;
        }
        return super.wasProcessedAsCompositeNode(node);
    }

    abstract void writeMountPoint(MountPointNode node) throws IOException;
}
