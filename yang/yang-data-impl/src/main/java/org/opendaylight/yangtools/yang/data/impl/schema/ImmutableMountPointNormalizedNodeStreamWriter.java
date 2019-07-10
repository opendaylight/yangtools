/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.collect.ClassToInstanceMap;
import java.io.IOException;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.ObjectExtensions;
import org.opendaylight.yangtools.concepts.ObjectExtensions.Factory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointNodeFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointNodeFactoryResolver;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointSchemaResolver;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointStreamWriter;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchema;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.AnydataExtension;
import org.opendaylight.yangtools.yang.data.api.schema.stream.ForwardingNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriterExtension;

// FIXME: document usage of this
@Beta
public abstract class ImmutableMountPointNormalizedNodeStreamWriter extends ImmutableNormalizedNodeStreamWriter
        implements MountPointStreamWriter {
    private static final Factory<ImmutableMountPointNormalizedNodeStreamWriter, ?, NormalizedNodeStreamWriterExtension>
        EXTENSIONS_FACTORY = ObjectExtensions.factory(ImmutableMountPointNormalizedNodeStreamWriter.class,
            AnydataExtension.class, MountPointStreamWriter.class);

    protected ImmutableMountPointNormalizedNodeStreamWriter(final NormalizedNodeResult result) {
        super(result);
    }

    @Override
    public final ClassToInstanceMap<NormalizedNodeStreamWriterExtension> getExtensions() {
        return EXTENSIONS_FACTORY.newInstance(this);
    }

    @Override
    public final Optional<MountPointSchemaResolver> findMountPoint(final MountPointIdentifier label) {
        return findResolver(label).map(factory -> factory);
    }

    @Override
    public final NormalizedNodeStreamWriter startMountPoint(final MountPointSchema mountSchema) {
        checkArgument(mountSchema instanceof MountPointNodeFactory, "Unsupported schema %s", mountSchema);
        final MountPointNodeFactory factory = (MountPointNodeFactory) mountSchema;

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

                final NormalizedNode<?, ?> data = mountResult.getResult();
                if (!(data instanceof ContainerNode)) {
                    throw new IOException("Unhandled mount data " + data);
                }

                writeChild(factory.createMountPoint((ContainerNode) data));
            }
        };
    }

    // XXX: this resolver must end up returning MountPointNodeFactory
    protected abstract Optional<MountPointNodeFactoryResolver> findResolver(MountPointIdentifier label);
}
