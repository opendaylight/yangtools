/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointLabel;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.stream.ForwardingNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MountPointExtension;
import org.opendaylight.yangtools.yang.data.util.ImmutableNormalizedMountPoint;
import org.opendaylight.yangtools.yang.data.util.ImmutableNormalizedMountpoints;
import org.opendaylight.yangtools.yang.data.util.ImmutableNormalizedMountpoints.Builder;

// FIXME: document usage of this
@Beta
public abstract class ImmutableMountPointNormalizedNodeStreamWriter extends ImmutableNormalizedNodeStreamWriter
        implements MountPointExtension {
    @NonNullByDefault
    private record BuilderEntry(PathArgument identifier, Builder builder) {
        BuilderEntry {
            requireNonNull(identifier);
            requireNonNull(builder);
        }
    }

    private final Deque<BuilderEntry> builders = new ArrayDeque<>();
    private final NormalizationResultHolder holder;

    protected ImmutableMountPointNormalizedNodeStreamWriter(final NormalizationResultHolder holder) {
        super(holder);
        this.holder = requireNonNull(holder);
    }

    @Override
    public final List<MountPointExtension> supportedExtensions() {
        return List.of(this);
    }

    @Override
    public final NormalizedNodeStreamWriter startMountPoint(final MountPointLabel label,
            final MountPointContext mountCtx) {
        final var current = builders.peek();
        checkState(current != null, "Attempted to emit mount point when no data is open");

        final var mountResult = new NormalizationResultHolder();
        final var mountDelegate = ImmutableNormalizedNodeStreamWriter.from(mountResult);

        return new ForwardingNormalizedNodeStreamWriter() {
            @Override
            protected NormalizedNodeStreamWriter delegate() {
                return mountDelegate;
            }

            @Override
            public void close() throws IOException {
                super.close();

                final var result = mountResult.getResult();
                final var data = result.data();
                if (!(data instanceof ContainerNode container)) {
                    throw new IOException("Unhandled mount data " + data);
                }

                current.builder.withMountPoint(new ImmutableNormalizedMountPoint(label, mountCtx, container,
                    result.metadata(), result.mountPoints()));
            }
        };
    }

    @Override
    @SuppressWarnings("rawtypes")
    final void enter(final PathArgument identifier, final NormalizedNodeBuilder next) {
        super.enter(identifier, next);
        builders.push(new BuilderEntry(identifier, ImmutableNormalizedMountpoints.builder()));
    }

    @Override
    public final void endNode() {
        super.endNode();

        final var last = builders.pop();
        final var mountPoints = last.builder.build();
        final var current = builders.peek();
        if (current != null) {
            if (mountPoints.mountPoint() != null || !mountPoints.getChildren().isEmpty()) {
                current.builder.withChild(last.identifier, mountPoints);
            }
        } else {
            // All done
            holder.setMountPoints(mountPoints);
        }
    }
}

