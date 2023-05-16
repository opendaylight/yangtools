/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMountPoint;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MountPointExtension;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;

/**
 * A {@link NormalizedMountPoint}-aware counterpart to {@link NormalizedNodeWriter}. Based on the backing writer's
 * capability it either forwards or filters NormalizedMountPoints.
 */
@Beta
public abstract class MountPointNormalizedNodeWriter extends NormalizedNodeWriter {
    private static final class Filtering extends MountPointNormalizedNodeWriter {
        Filtering(final NormalizedNodeStreamWriter writer) {
            super(writer);
        }

        @Override
        void writeMountPoint(final NormalizedMountPoint mountPoint) {
            // No-op
        }
    }

    private static final class Forwarding extends MountPointNormalizedNodeWriter {
        private final MountPointExtension mountWriter;

        Forwarding(final NormalizedNodeStreamWriter writer, final MountPointExtension mountWriter) {
            super(writer);
            this.mountWriter = requireNonNull(mountWriter);
        }

        @Override
        void writeMountPoint(final NormalizedMountPoint mountPoint) throws IOException {
            try (var writer = forStreamWriter(mountWriter.startMountPoint(mountPoint.label(), mountPoint.context()))) {
                for (var child : mountPoint.children().values()) {
                    writer.write(child);
                }
            }
        }
    }

    MountPointNormalizedNodeWriter(final NormalizedNodeStreamWriter writer) {
        super(writer);
    }

    public static @NonNull MountPointNormalizedNodeWriter forStreamWriter(final NormalizedNodeStreamWriter writer) {
        final var mountWriter = writer.extension(MountPointExtension.class);
        return mountWriter == null ? new Filtering(writer) : new Forwarding(writer, mountWriter);
    }

    public static @NonNull MountPointNormalizedNodeWriter filteringFor(final NormalizedNodeStreamWriter writer) {
        return new Filtering(writer);
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
        justification = "SpotBugs does not grok checkArgument()")
    public static @NonNull MountPointNormalizedNodeWriter forwardingFor(final NormalizedNodeStreamWriter writer) {
        final var mountWriter = writer.extension(MountPointExtension.class);
        checkArgument(mountWriter != null, "Writer %s does not support mount points", writer);
        return new Forwarding(writer, mountWriter);
    }

    @Override
    protected final boolean wasProcessedAsCompositeNode(final NormalizedNode node) throws IOException {
        if (node instanceof NormalizedMountPoint mountPoint) {
            writeMountPoint(mountPoint);
            return true;
        }
        return super.wasProcessedAsCompositeNode(node);
    }

    abstract void writeMountPoint(NormalizedMountPoint mountPoint) throws IOException;
}
