/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.api;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.rfc8528.data.api.YangLibraryConstants.ContainerName;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;

/**
 * An entity able to resolve the SchemaContext for embedded mount points based on generic data provided by the current
 * interpretation context.
 */
@Beta
@NonNullByDefault
public interface MountPointContextFactory {
    /**
     * Create a mount point context based on available information. Implementations are expected to attempt to interpret
     * provided data to their best of their ability.
     *
     * @param libraryContainers available YANG library containers in opaque format
     * @param schemaMounts the content of 'schema-mounts' container, if available
     * @return A {@link MountPointContext}
     * @throws YangParserException if the schema cannot be assembled
     */
    MountPointContext createContext(Map<ContainerName, MountPointChild> libraryContainers,
            @Nullable MountPointChild schemaMounts) throws YangParserException;
}
