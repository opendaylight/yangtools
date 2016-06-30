/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.IndexStrategy;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

abstract class UniqueIndexStrategyBase implements IndexStrategy {
    static UniqueIndexStrategyBase forList(final ListSchemaNode schema, final DataTreeConfiguration config) {
        switch (config.getTreeType()) {
        case CONFIGURATION:
            return !config.isUniqueIndexEnabled() || schema.getUniqueConstraints().isEmpty() ?
                    NoOpUniqueIndexStrategy.INSTANCE :
                        new StrictUniqueIndexStrategy(schema.getUniqueConstraints());
        case OPERATIONAL:
            return NoOpUniqueIndexStrategy.INSTANCE;
        default:
            throw new UnsupportedOperationException(String.format("Not supported tree type %s", config.getTreeType()));
        }
    }

    abstract protected List<Set<YangInstanceIdentifier>> getUniqueConstraintsLeafIds();
}
