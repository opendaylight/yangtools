/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * AnyXML node with schema of contained XML data.
 */
@Beta
public interface YangModeledAnyXmlNode extends DataContainerNode<NodeIdentifier>,
        DataContainerChild<NodeIdentifier, Collection<DataContainerChild<? extends PathArgument, ?>>> {

    /**
     * Returns the schema corresponding to the node's datea.
     *
     * @return schema of contained XML data
     */
    @NonNull ContainerSchemaNode getSchemaOfAnyXmlData();
}
