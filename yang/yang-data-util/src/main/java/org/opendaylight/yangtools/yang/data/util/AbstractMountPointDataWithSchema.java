/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.rfc8528.model.api.YangLibraryConstants.ContainerName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizableAnydata;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * A {@link CompositeNodeDataWithSchema} which can hold mount-point data.
 */
@Beta
public abstract class AbstractMountPointDataWithSchema<T extends DataSchemaNode>
        extends CompositeNodeDataWithSchema<T> {
    private static final class MountPointState {
        private final Map<ContainerName, NormalizableAnydata> yangLibContainers = new EnumMap<>(ContainerName.class);
        private final QName label;

        MountPointState(final QName label) {
            this.label = requireNonNull(label);
        }
    }

    private MountPointState state;

    AbstractMountPointDataWithSchema(final T schema) {
        super(schema);
    }
}
