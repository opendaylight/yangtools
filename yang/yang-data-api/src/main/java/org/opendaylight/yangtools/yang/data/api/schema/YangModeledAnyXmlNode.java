/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * AnyXML node with schema of contained XML data.
 */
public interface YangModeledAnyXmlNode extends AttributesContainer,
        DataContainerChild<NodeIdentifier, DataContainerChild<?, ?>> {

    /**
     * @return DataSchemaNode - schema of contained XML data
     */
    DataSchemaNode getSchemaOfAnyXmlData();
}
