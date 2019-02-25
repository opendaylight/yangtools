/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import java.io.IOException;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * childs - empty augment - only one element can be.
 */
class ChoiceNodeDataWithSchema extends CompositeNodeDataWithSchema<ChoiceSchemaNode> {
    private CaseNodeDataWithSchema caseNodeDataWithSchema;

    ChoiceNodeDataWithSchema(final ChoiceSchemaNode schema) {
        super(schema);
    }

    @Override
    protected CaseNodeDataWithSchema addCompositeChild(final DataSchemaNode schema) {
        CaseNodeDataWithSchema newChild = new CaseNodeDataWithSchema((CaseSchemaNode) schema);
        caseNodeDataWithSchema = newChild;
        addCompositeChild(newChild);
        return newChild;
    }

    public CaseNodeDataWithSchema getCase() {
        return caseNodeDataWithSchema;
    }

    @Override
    public void write(final NormalizedNodeStreamWriter writer) throws IOException {
        writer.nextDataSchemaNode(getSchema());
        writer.startChoiceNode(provideNodeIdentifier(), childSizeHint());
        super.write(writer);
        writer.endNode();
    }
}
