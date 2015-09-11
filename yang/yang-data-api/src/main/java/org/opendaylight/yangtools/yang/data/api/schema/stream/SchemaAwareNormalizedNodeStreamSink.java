/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import org.opendaylight.yangtools.yang.model.api.SchemaNode;

/**
 * An extension of {@link NormalizedNodeStreamSink}, which can performs schema-informed processing. Users of this
 * interface can inform the sink of the {@link SchemaNode} of the element which is about be started via
 * {@link #nextNodeSchema(SchemaNode)}.
 */
public interface SchemaAwareNormalizedNodeStreamSink extends NormalizedNodeStreamSink {

    void nextNodeSchema(SchemaNode schema);
}
