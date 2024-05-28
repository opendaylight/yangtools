/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMountPoint;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MountPointExtension;

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
                // FIXME: this does not deal with metadata nor nested mount points. For that we need a
                //        writer.write(NormalizedTree) method, which in turn requires more integration.
                writer.write(mountPoint.data());
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
