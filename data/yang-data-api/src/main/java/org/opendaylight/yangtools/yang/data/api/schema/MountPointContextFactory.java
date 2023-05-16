/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An entity able to resolve the SchemaContext for embedded mount points based on generic data provided by the current
 * interpretation context.
 */
@Beta
@NonNullByDefault
public interface MountPointContextFactory {
    /**
     * Top-level containers which hold YANG Library information, ordered by descending preference, with more modern
     * and/or preferred entries first.
     */
    enum ContainerName {
        // Note: order this enum from most-preferred to least-preferred name
        /**
         * Container in RFC8525 (NMDA) YANG Library.
         */
        RFC8525("yang-library"),
        /**
         * Container in RFC7895 (pre-NMDA) YANG Library.
         */
        RFC7895("modules-state");

        private final String localName;

        ContainerName(final String localName) {
            this.localName = requireNonNull(localName);
        }

        public String getLocalName() {
            return localName;
        }

        public static ContainerName ofLocalName(final String localName) {
            final var ret = forLocalName(localName);
            if (ret == null) {
                throw new IllegalArgumentException("Unrecognized container name '" + localName + "'");
            }
            return ret;
        }

        public static @Nullable ContainerName forLocalName(final String localName) {
            return switch (localName) {
                case "yang-library" -> RFC8525;
                case "modules-state" -> RFC7895;
                default -> null;
            };
        }
    }

    /**
     * Create a mount point context based on available information. Implementations are expected to attempt to interpret
     * provided data to their best of their ability.
     *
     * @param libraryContainers available YANG library containers in opaque format
     * @param schemaMounts the content of 'schema-mounts' container, if available
     * @return A {@link MountPointContext}
     * @throws MountPointException if the schema cannot be assembled
     */
    MountPointContext createContext(Map<ContainerName, MountPointChild> libraryContainers,
            @Nullable MountPointChild schemaMounts) throws MountPointException;
}
