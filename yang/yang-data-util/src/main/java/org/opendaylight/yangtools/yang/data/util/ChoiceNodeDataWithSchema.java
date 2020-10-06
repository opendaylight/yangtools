/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Verify.verify;

import java.io.IOException;
import org.opendaylight.yangtools.rfc7952.data.api.StreamWriterMetadataExtension;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * childs - empty augment - only one element can be.
 */
final class ChoiceNodeDataWithSchema extends CompositeNodeDataWithSchema<ChoiceSchemaNode> {
    private CaseNodeDataWithSchema caseNodeDataWithSchema;

    ChoiceNodeDataWithSchema(final ChoiceSchemaNode schema) {
        super(schema);
    }

    // FIXME: 7.0.0: this should be impossible to hit
    @Override
    CaseNodeDataWithSchema addCompositeChild(final DataSchemaNode schema, final ChildReusePolicy policy) {
        verify(schema instanceof CaseSchemaNode, "Unexpected schema %s", schema);
        return addCompositeChild((CaseSchemaNode) schema, policy);
    }

    CaseNodeDataWithSchema addCompositeChild(final CaseSchemaNode schema, final ChildReusePolicy policy) {
        CaseNodeDataWithSchema newChild = new CaseNodeDataWithSchema(schema);
        caseNodeDataWithSchema = newChild;
        addCompositeChild(newChild, policy);
        return newChild;
    }

    CaseNodeDataWithSchema getCase() {
        return caseNodeDataWithSchema;
    }

    @Override
    public void write(final NormalizedNodeStreamWriter writer, final StreamWriterMetadataExtension metaWriter)
            throws IOException {
        writer.nextDataSchemaNode(getSchema());
        writer.startChoiceNode(provideNodeIdentifier(), childSizeHint());
        super.write(writer, metaWriter);
        writer.endNode();
    }
}
