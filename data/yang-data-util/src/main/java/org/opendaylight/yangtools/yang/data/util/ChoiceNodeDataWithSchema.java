/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.base.VerifyException;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MetadataExtension;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree.Step;
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

    @Override
    AbstractNodeDataWithSchema<?> enterChild(final Step step, final QName child, final ChildReusePolicy policy) {
        if (step instanceof Step.InCase(var childContext, var inCase)) {
            final var caseChild = new CaseNodeDataWithSchema(inCase.toDataSchemaNode());
            addCompositeChild(caseChild, policy);
            return caseChild.addCompositeChild(childContext.dataSchemaNode(), policy);
        }
        throw new VerifyException("Unexpected step " + step);
    }


    // FIXME: 7.0.0: this should be impossible to hit
    @Override
    CaseNodeDataWithSchema addCompositeChild(final DataSchemaNode schema, final ChildReusePolicy policy) {
        if (schema instanceof CaseSchemaNode caseSchema) {
            return addCompositeChild(caseSchema, policy);
        }
        throw new VerifyException("Unexpected schema " + schema);
    }

    @NonNullByDefault
    CaseNodeDataWithSchema addCompositeChild(final CaseSchemaNode schema, final ChildReusePolicy policy) {
        var newChild = new CaseNodeDataWithSchema(schema);
        caseNodeDataWithSchema = newChild;
        addCompositeChild(newChild, policy);
        return newChild;
    }

    CaseNodeDataWithSchema getCase() {
        return caseNodeDataWithSchema;
    }

    @Override
    public void write(final NormalizedNodeStreamWriter writer, final MetadataExtension metaWriter) throws IOException {
        writer.nextDataSchemaNode(getSchema());
        writer.startChoiceNode(provideNodeIdentifier(), childSizeHint());
        super.write(writer, metaWriter);
        writer.endNode();
    }
}
