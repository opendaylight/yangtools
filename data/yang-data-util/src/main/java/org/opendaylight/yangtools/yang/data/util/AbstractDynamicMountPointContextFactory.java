/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractSimpleIdentifiable;
import org.opendaylight.yangtools.yang.common.MountPointLabel;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointChild;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContextFactory;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointException;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for dynamic resolvers.
 */
@Beta
// FIXME: 7.0.0: consider integrating into AbstractMountPointContextFactory
public abstract class AbstractDynamicMountPointContextFactory extends AbstractSimpleIdentifiable<MountPointLabel>
        implements MountPointContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDynamicMountPointContextFactory.class);

    protected AbstractDynamicMountPointContextFactory(final @NonNull MountPointLabel label) {
        super(label);
    }

    @Override
    public final MountPointContext createContext(final Map<ContainerName, MountPointChild> libraryContainers,
            final MountPointChild schemaMounts) throws MountPointException {

        for (Entry<ContainerName, MountPointChild> entry : libraryContainers.entrySet()) {
            // Context for the specific code word
            final Optional<EffectiveModelContext> optLibContext = findSchemaForLibrary(entry.getKey());
            if (optLibContext.isEmpty()) {
                LOG.debug("YANG Library context for mount point {} container {} not found", getIdentifier(),
                    entry.getKey());
                continue;
            }

            final NormalizedNode libData;
            try {
                libData = entry.getValue().normalizeTo(optLibContext.orElseThrow());
            } catch (IOException e) {
                throw new MountPointException("Failed to interpret yang-library data", e);
            }
            if (!(libData instanceof ContainerNode libContainer)) {
                throw new MountPointException("Invalid yang-library non-container " + libData);
            }

            final EffectiveModelContext schemaContext = bindLibrary(entry.getKey(), libContainer);
            if (schemaMounts == null) {
                return MountPointContext.of(schemaContext);
            }

            final NormalizedNode mountData;
            try {
                mountData = schemaMounts.normalizeTo(schemaContext);
            } catch (IOException e) {
                throw new MountPointException("Failed to interpret schema-mount data", e);
            }
            if (!(mountData instanceof ContainerNode)) {
                throw new MountPointException("Invalid schema-mount non-container " + mountData);
            }

            return createMountPointContext(schemaContext, (ContainerNode) mountData);
        }

        throw new MountPointException("Failed to interpret " + libraryContainers);
    }

    protected abstract @NonNull MountPointContext createMountPointContext(@NonNull EffectiveModelContext schemaContext,
            @NonNull ContainerNode mountData);

    /**
     * Assemble the MountPointContext for specified normalized YANG Library top-level container.
     *
     * @param containerName Top-level YANG Library container
     * @param libData Top-level YANG Library container data
     * @return An assembled MountPointContext
     * @throws NullPointerException if container is null
     * @throws MountPointException if the schema context cannot be assembled
     */
    protected abstract @NonNull EffectiveModelContext bindLibrary(@NonNull ContainerName containerName,
            @NonNull ContainerNode libData) throws MountPointException;

    /**
     * Return the schema in which YANG Library container content should be interpreted.
     *
     * <p>Note this schema is not guaranteed to contain any augmentations, hence parsing could fail.
     *
     * @param containerName Top-level YANG Library container name
     * @return The LibraryContext to use when interpreting the specified YANG Library container, or empty
     * @throws NullPointerException if container is null
     */
    protected abstract Optional<EffectiveModelContext> findSchemaForLibrary(@NonNull ContainerName containerName);
}
