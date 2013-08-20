/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.opendaylight.yangtools.yang.common.QName;

public interface UnknownSchemaNode extends SchemaNode {

    QName getNodeType();

    String getNodeParameter();

    boolean isAddedByUses();

    /**
     * Get extension definition which identifies this node
     *
     * @return extension definition if exists, null otherwise
     */
    ExtensionDefinition getExtensionDefinition();

}
