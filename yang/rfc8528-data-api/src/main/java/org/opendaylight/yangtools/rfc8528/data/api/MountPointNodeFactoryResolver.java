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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchemaResolver;
import org.opendaylight.yangtools.rfc8528.model.api.StaticMountPointSchemaResolver;
import org.opendaylight.yangtools.rfc8528.model.api.YangLibraryConstants.ContainerName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;

@Beta
public interface MountPointNodeFactoryResolver extends MountPointSchemaResolver {
    /**
     * A resolver which can resolve the SchemaContext for use with mount point data based on the
     * {@code ietf-yang-library} content of the mountpoint itself. This process requires two steps:
     * <ul>
     *   <li>{@link #findSchemaForLibrary(ContainerName)} is invoked to acquire a SchemaContext in which to interpret
     *       one of the possible {@code ietf-yang-library} top-level containers.
     *   </li>
     *   <li>The container is normalized based on the returned context by the user of this interface and then
     *       {@link LibraryContext#bindTo(ContainerNode)} is invoked to acquire the MountPointMetadata.
     *   </li>
     * </ul>
     */
    public interface Inline extends MountPointNodeFactoryResolver {
        @NonNullByDefault
        interface LibraryContext {
            /**
             * Return a SchemaContext capable of parsing the content of YANG Library.
             *
             * @return A SchemaContext instance
             */
            SchemaContext getLibraryContainerSchema();

            /**
             * Assemble the SchemaContext for specified normalized YANG Library top-level container.
             *
             * @param container Top-level YANG Library container
             * @return An assembled SchemaContext
             * @throws NullPointerException if container is null
             * @throws YangParserException if the schema context cannot be assembled
             */
            MountPointNodeFactory bindTo(ContainerNode container) throws YangParserException;
        }

        /**
         * Return the schema in which YANG Library container content should be interpreted.
         *
         * <p>
         * Note this schema is not guaranteed to contain any augmentations, hence parsing could fail.
         *
         * @param containerName Top-level YANG Library container name
         * @return The LibraryContext to use when interpreting the specified YANG Library container, or empty
         * @throws NullPointerException if container is null
         */
        Optional<LibraryContext> findSchemaForLibrary(@NonNull ContainerName containerName);
    }

    @NonNullByDefault
    interface SharedSchema extends MountPointNodeFactoryResolver, StaticMountPointSchemaResolver {

        @Override
        MountPointNodeFactory getSchema();
    }
}
