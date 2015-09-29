/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * AnyXML node with schema.
 */
public interface AnyXmlNodeWithSchema extends AnyXmlNode {

    /**
     * @return DataSchemaNode - schema of XML data
     */
    DataSchemaNode getSchema();

    /**
     * @return NormalizedNode representation of XML data
     */
    NormalizedNode<?,?> getAsNormalizedNode();
}
