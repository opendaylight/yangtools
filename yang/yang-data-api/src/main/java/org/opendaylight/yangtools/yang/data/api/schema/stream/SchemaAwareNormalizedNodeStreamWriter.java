/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Marker interface for {@link NormalizedNodeStreamWriter}s which can take advantage of {@link DataSchemaNode}
 * information when writing the nodes.
 */
@Beta
public interface SchemaAwareNormalizedNodeStreamWriter extends NormalizedNodeStreamWriter, DataSchemaNodeAware {

    void startYangModeledAnyXmlNode(NodeIdentifier provideNodeIdentifier, int childSizeHint);

}
