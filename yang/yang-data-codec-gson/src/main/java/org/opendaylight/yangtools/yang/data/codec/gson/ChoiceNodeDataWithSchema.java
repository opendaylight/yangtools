/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.UNKNOWN_SIZE;

import java.io.IOException;

import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 *
 * childs - empty augment - only one element can be
 *
 */
class ChoiceNodeDataWithSchema extends CompositeNodeDataWithSchema {

    private CaseNodeDataWithSchema caseNodeDataWithSchema;

    public ChoiceNodeDataWithSchema(final ChoiceNode schema) {
        super(schema);
    }

    @Override
    public CompositeNodeDataWithSchema addCompositeChild(final DataSchemaNode schema) {
        CaseNodeDataWithSchema newChild = new CaseNodeDataWithSchema((ChoiceCaseNode) schema);
        caseNodeDataWithSchema = newChild;
        addCompositeChild(newChild);
        return newChild;
    }

    public CaseNodeDataWithSchema getCase() {
        return caseNodeDataWithSchema;
    }

    @Override
    protected void writeToStream(final NormalizedNodeStreamWriter nnStreamWriter) throws IOException {
        nnStreamWriter.startChoiceNode(provideNodeIdentifier(), UNKNOWN_SIZE);
        super.writeToStream(nnStreamWriter);
        nnStreamWriter.endNode();
    }

}
