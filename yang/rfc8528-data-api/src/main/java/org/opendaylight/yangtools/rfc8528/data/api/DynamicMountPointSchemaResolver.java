/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchemaResolver;
import org.opendaylight.yangtools.rfc8528.model.api.YangLibraryConstants.ContainerName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;

/**
 * A resolver which can resolve the SchemaContext for use with mount point data based on the
 * {@code ietf-yang-library} content of the mountpoint itself. This process requires two steps:
 * <ul>
 *   <li>{@link #findContainerContext(ContainerName)} is invoked to acquire a SchemaContext in which to interpret
 *       one of the possible {@code ietf-yang-library} top-level containers.
 *   </li>
 *   <li>The container is normalized based on the returned context by the user of this interface and then
 *       {@link #assembleSchemaContext(ContainerNode)} is invoked to acquire the SchemaContext which will be used
 *       to interpret the mount point data.
 *   </li>
 * </ul>
 */
@Beta
public interface DynamicMountPointSchemaResolver extends MountPointSchemaResolver {
    /**
     * Return the schema in which YANG Library container content should be interpreted.
     *
     * @param containerName Top-level YANG Library container name
     * @return The SchemaContext to use when interpreting the specified YANG Library container, or empty
     * @throws NullPointerException if container is null
     */
    Optional<SchemaContext> findContainerContext(@NonNull ContainerName containerName);

    /**
     * Assemble the SchemaContext for specified normalized YANG Library top-level container.
     *
     * @param container Top-level YANG Library container
     * @return An assembled SchemaContext
     * @throws NullPointerException if container is null
     * @throws YangParserException if the schema context cannot be assembled
     */
    @NonNull SchemaContext assembleSchemaContext(@NonNull ContainerNode container) throws YangParserException;
}
