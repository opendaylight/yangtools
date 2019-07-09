/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.rfc8528.data.api.DynamicMountPointSchemaResolver;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointStreamWriter;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchemaResolver;
import org.opendaylight.yangtools.rfc8528.model.api.StaticMountPointSchemaResolver;
import org.opendaylight.yangtools.rfc8528.model.api.YangLibraryConstants.ContainerName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizableAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YANG Schema Mount-supported data attached to either a {@code list} item or a {@code container}.
 */
@Beta
public final class MountPointData implements Identifiable<QName> {
    private static final Logger LOG = LoggerFactory.getLogger(MountPointData.class);

    private final Map<ContainerName, NormalizableAnydata> yangLibContainers = new EnumMap<>(ContainerName.class);
    private final @NonNull QName label;

    MountPointData(final QName label) {
        this.label = requireNonNull(label);
    }

    @Override
    public QName getIdentifier() {
        return label;
    }

    void write(final @NonNull NormalizedNodeStreamWriter writer) {
        final MountPointStreamWriter mountWriter = writer.getExtensions().getInstance(MountPointStreamWriter.class);
        if (mountWriter == null) {
            LOG.debug("Writer {} does not support mount points, ignoring data in {}", writer, label);
            return;
        }

        final Optional<MountPointSchemaResolver> optResolver = mountWriter.findMountPoint(label);
        if (!optResolver.isPresent()) {
            LOG.debug("Mount point for {} is not present, ignoring it", label);
            return;
        }

        final MountPointSchemaResolver resolver = optResolver.get();
        if (resolver instanceof StaticMountPointSchemaResolver) {
            writeStatic(writer, mountWriter, ((StaticMountPointSchemaResolver) resolver).getSchemaContext());
        } else if (resolver instanceof DynamicMountPointSchemaResolver) {
            writeDynamic(writer, mountWriter, (DynamicMountPointSchemaResolver) resolver);
        } else {
            throw new IllegalStateException("Unhandled resolver  ");
        }
    }

    private void writeDynamic(final NormalizedNodeStreamWriter writer,
            final @NonNull MountPointStreamWriter mountWriter, final DynamicMountPointSchemaResolver resolver) {
        // FIXME: YANGTOOLS-1007: negotiate/emit data
    }

    private void writeStatic(final NormalizedNodeStreamWriter writer, final @NonNull MountPointStreamWriter mountWriter,
            final @NonNull SchemaContext schemaContext) {
        // FIXME: YANGTOOLS-1007: negotiate/emit data
    }

}