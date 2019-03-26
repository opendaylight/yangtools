/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.collect.ImmutableMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class ChoiceNodeContextNode extends AbstractMixinContextNode<NodeIdentifier> {
    private final ImmutableMap<QName, DataSchemaContextNode<?>> byQName;
    private final ImmutableMap<PathArgument, DataSchemaContextNode<?>> byArg;

    ChoiceNodeContextNode(final ChoiceSchemaNode schema) {
        super(NodeIdentifier.create(schema.getQName()), schema);
        ImmutableMap.Builder<QName, DataSchemaContextNode<?>> byQNameBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<PathArgument, DataSchemaContextNode<?>> byArgBuilder = ImmutableMap.builder();

        for (CaseSchemaNode caze : schema.getCases().values()) {
            for (DataSchemaNode cazeChild : caze.getChildNodes()) {
                DataSchemaContextNode<?> childOp = fromDataSchemaNode(cazeChild);
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
}
