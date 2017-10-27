/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Optional;

/**
 * Common interface for {@link DataSchemaNode}s which contain data modeled potentially outside of this domain. Such
 * nodes can optionally make the underlying data model available via {@link #getDataSchema()}.
 *
 * @author Robert Varga
 */
public interface ForeignDataSchemaAware {
    /**
     * Schema of data.
     *
     * @return schema of contained data or empty if it is not provided
     */
    Optional<ContainerSchemaNode> getDataSchema();
}
