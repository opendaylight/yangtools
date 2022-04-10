/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

@Beta
@Deprecated(forRemoval = true, since = "8.0.3")
public abstract class DataNodeAggregator {
    protected void addChild(final DataSchemaNode childNode) {
        // No-op by default
    }

    protected void addContainer(final ContainerSchemaNode containerNode) {
        // No-op by default
    }

    protected void addList(final ListSchemaNode list) {
        // No-op by default
    }

    protected void addChoice(final ChoiceSchemaNode choiceNode) {
        // No-op by default
    }

    protected void addTypedefs(final Collection<? extends TypeDefinition<?>> typeDefs) {
        // No-op by default
    }

    protected void addGrouping(final GroupingDefinition grouping) {
        // No-op by default
    }
}
