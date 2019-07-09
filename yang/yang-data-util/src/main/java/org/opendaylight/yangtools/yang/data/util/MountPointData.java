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
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.rfc8528.data.api.DynamicMountPointSchemaResolver;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointStreamWriter;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchemaResolver;
import org.opendaylight.yangtools.rfc8528.model.api.StaticMountPointSchemaResolver;
import org.opendaylight.yangtools.rfc8528.model.api.YangLibraryConstants.ContainerName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YANG Schema Mount-supported data attached to either a {@code list} item or a {@code container}.
 */
@Beta
public final class MountPointData extends AbstractIdentifiable<QName> {
    private static final Logger LOG = LoggerFactory.getLogger(MountPointData.class);

    private final Map<ContainerName, MountPointChild> yangLib = new EnumMap<>(ContainerName.class);
    private final List<MountPointChild> children = new ArrayList<>();

    MountPointData(final QName label) {
        super(label);
    }

    public void setContainer(final @NonNull ContainerName containerName, final @NonNull MountPointChild data) {
        final MountPointChild prev = yangLib.putIfAbsent(containerName, requireNonNull(data));
        checkState(prev == null, "Attempted to duplicate container %s data %s with %s", containerName, prev, data);
        addChild(data);
    }

    public void addChild(final @NonNull MountPointChild data) {
        children.add(requireNonNull(data));
    }

    void write(final @NonNull NormalizedNodeStreamWriter writer) throws IOException {
        final MountPointStreamWriter mountWriter = writer.getExtensions().getInstance(MountPointStreamWriter.class);
        if (mountWriter == null) {
            LOG.debug("Writer {} does not support mount points, ignoring data in {}", writer, getIdentifier());
            return;
        }

        final Optional<MountPointSchemaResolver> optResolver = mountWriter.findMountPoint(getIdentifier());
        if (!optResolver.isPresent()) {
            LOG.debug("Mount point for {} is not present, ignoring it", getIdentifier());
            return;
        }

        final MountPointSchemaResolver resolver = optResolver.get();
        if (resolver instanceof StaticMountPointSchemaResolver) {
            writeStatic(mountWriter, ((StaticMountPointSchemaResolver) resolver).getSchemaContext());
        } else if (resolver instanceof DynamicMountPointSchemaResolver) {
            writeDynamic(mountWriter, (DynamicMountPointSchemaResolver) resolver);
        } else {
            throw new IOException("Unhandled resolver " + resolver);
        }
    }

    private void writeDynamic(final @NonNull MountPointStreamWriter mountWriter,
            final DynamicMountPointSchemaResolver resolver) throws IOException {
        for (Entry<ContainerName, MountPointChild> entry : yangLib.entrySet()) {
            final Optional<SchemaContext> optContext = resolver.findContainerContext(entry.getKey());
            if (!optContext.isPresent()) {
                LOG.debug("YANG Library context for mount point {} container {} not found", getIdentifier(),
                    entry.getKey());
                continue;
            }

            final NormalizedNode<?, ?> data = entry.getValue().normalizeTo(optContext.get());
            if (!(data instanceof ContainerNode)) {
                throw new IOException("Invalid non-container " + data);
            }

            final SchemaContext context;
            try {
                context = resolver.assembleSchemaContext((ContainerNode) data);
            } catch (YangParserException e) {
                throw new IOException("Failed to assemble context for " + data, e);
            }

            writeStatic(mountWriter, context);
            return;
        }

        LOG.warn("Failed to create a dynamic context for mount point {}, ignoring its data", getIdentifier());
    }

    private void writeStatic(final @NonNull MountPointStreamWriter mountWriter,
            final @NonNull SchemaContext schemaContext) throws IOException {
        final Optional<NormalizedNodeStreamWriter> optWriter = mountWriter.startMountPoint(getIdentifier(),
            schemaContext);
        if (!optWriter.isPresent()) {
            LOG.debug("Ignoring mount point {} data due to writer decision", getIdentifier());
            return;
        }

        try (NormalizedNodeStreamWriter writer = optWriter.get()) {
            for (MountPointChild child : children) {
                child.writeTo(writer, schemaContext);
            }
        }
    }
}
