/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.collect.ImmutableMap;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class ChoiceNodeContextNode extends AbstractMixinContextNode<NodeIdentifier> {
    private final ImmutableMap<QName, DataSchemaContextNode<?>> byQName;
    private final ImmutableMap<PathArgument, DataSchemaContextNode<?>> byArg;

    ChoiceNodeContextNode(final ChoiceSchemaNode schema) {
        super(NodeIdentifier.create(schema.getQName()), schema);
        ImmutableMap.Builder<QName, DataSchemaContextNode<?>> byQNameBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<PathArgument, DataSchemaContextNode<?>> byArgBuilder = ImmutableMap.builder();

        for (CaseSchemaNode caze : schema.getCases()) {
            for (DataSchemaNode cazeChild : caze.getChildNodes()) {
                // FIXME: this seems to be wrong, as it does not handle:
                //
                // choice one {
                //   case two {
                //     choice three {
                //       leaf four {
                //         type string;
                //       }
                //     }
                //   }
                // }
                //
                // The problem is that 'byQName' will point to 'choice three' -- hence the 'byQName' lookup is not
                // correct from either 'schema tree' nor 'data tree' perspective.
                DataSchemaContextNode<?> childOp = DataSchemaContextNode.of(cazeChild);
                byArgBuilder.put(childOp.getIdentifier(), childOp);
                for (QName qname : childOp.getQNameIdentifiers()) {
                    byQNameBuilder.put(qname, childOp);
                }
            }
        }
        byQName = byQNameBuilder.build();
        byArg = byArgBuilder.build();
    }

    @Override
    public DataSchemaContextNode<?> getChild(final PathArgument child) {
        return byArg.get(child);
    }

    @Override
    public DataSchemaContextNode<?> getChild(final QName child) {
        return byQName.get(child);
    }

    @Override
    protected Set<QName> getQNameIdentifiers() {
        return byQName.keySet();
    }

    @Override
    protected DataSchemaContextNode<?> enterChild(final QName qname, final SchemaInferenceStack stack) {
        final var result = getChild(qname);
        if (result != null) {
            // FIXME: implement this
            throw new UnsupportedOperationException();
        }
        return result;
    }
}
