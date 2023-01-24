/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import com.google.common.annotations.Beta;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import java.io.IOException;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContext;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.rfc8528.data.api.StreamWriterMountPointExtension;
import org.opendaylight.yangtools.rfc8528.data.util.ImmutableMountPointNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.ForwardingNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriterExtension;

// FIXME: document usage of this
@Beta
public abstract class ImmutableMountPointNormalizedNodeStreamWriter extends ImmutableNormalizedNodeStreamWriter
        implements StreamWriterMountPointExtension {
    protected ImmutableMountPointNormalizedNodeStreamWriter(final NormalizedNodeResult result) {
        super(result);
    }

    @Override
    public final ClassToInstanceMap<NormalizedNodeStreamWriterExtension> getExtensions() {
        return ImmutableClassToInstanceMap.of(StreamWriterMountPointExtension.class, this);
    }

    @Override
    public final NormalizedNodeStreamWriter startMountPoint(final MountPointIdentifier mountId,
            final MountPointContext mountCtx) {
        final NormalizedNodeResult mountResult = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter mountDelegate = ImmutableNormalizedNodeStreamWriter.from(mountResult);

        return new ForwardingNormalizedNodeStreamWriter() {
            @Override
            protected NormalizedNodeStreamWriter delegate() {
                return mountDelegate;
            }

            @Override
            public void close() throws IOException {
                super.close();

                final var data = mountResult.getResult();
                if (!(data instanceof ContainerNode container)) {
                    throw new IOException("Unhandled mount data " + data);
                }

                writeChild(ImmutableMountPointNode.of(mountId, mountCtx, container));
            }
        };
    }
}
