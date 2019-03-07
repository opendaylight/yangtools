/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.model.api;

import com.google.common.annotations.Beta;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * Represents the effect of 'mount-point' extension, as defined in
 * <a href="https://tools.ietf.org/html/rfc8528">RFC8528</a>, being attached to a SchemaNode.
 */
@Beta
public interface MountPointSchemaNode extends UnknownSchemaNode {
    /**
     * Find all mount points defined in a {@link ContainerSchemaNode}.
     *
     * @param schema ContainerSchemaNode to search
     * @return {@link MountPointSchemaNode}s defined the ContainerSchemaNode.
     * @throws NullPointerException if context is null
     */
    static @NonNull Stream<MountPointSchemaNode> streamAll(final ContainerSchemaNode schema) {
        return schema.getUnknownSchemaNodes().stream()
                .filter(MountPointSchemaNode.class::isInstance)
                .map(MountPointSchemaNode.class::cast);
    }

    /**
     * Find all mount points defined in a {@link ListSchemaNode}.
     *
     * @param schema ListSchemaNode to search
     * @return {@link MountPointSchemaNode}s defined the ListSchemaNode.
     * @throws NullPointerException if context is null
     */
    static @NonNull Stream<MountPointSchemaNode> streamAll(final ListSchemaNode schema) {
        return schema.getUnknownSchemaNodes().stream()
                .filter(MountPointSchemaNode.class::isInstance)
                .map(MountPointSchemaNode.class::cast);
    }
}
