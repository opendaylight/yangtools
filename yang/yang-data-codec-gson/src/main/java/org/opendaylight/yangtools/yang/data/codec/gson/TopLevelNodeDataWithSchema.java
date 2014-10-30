/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * A node which holds top level element.
 */
class TopLevelNodeDataWithSchema extends CompositeNodeDataWithSchema {

    public TopLevelNodeDataWithSchema(DataSchemaNode schema) {
        super(schema);
    }

    /**
     * If direct child is of type ListNodeDataWithSchema its direct child - ListEntryNodeDataWitchSchema - is returned.
     *
     * ListEntry node should be returned insteand List at top level.
     */
    void normalizeTopLevelNode() {
        if (children.size() == 1 && children.get(0) instanceof ListNodeDataWithSchema) {
            List<AbstractNodeDataWithSchema> listEntry = ((ListNodeDataWithSchema) children.get(0)).getListEntry();
            if (listEntry.size() == 1) {
                children.remove(0);
                children.add(listEntry.get(0));
            } else {
                throw new IllegalStateException("Top level \"list\" element should contain only one list entry.");
            }
        }
    }

}
