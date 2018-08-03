/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;

final class AugmentationContextNode extends DataContainerContextNode<AugmentationIdentifier> {
    AugmentationContextNode(final AugmentationSchemaNode augmentation, final DataNodeContainer schema) {
        super(augmentationIdentifierFrom(augmentation), EffectiveAugmentationSchema.create(augmentation, schema), null);
    }

    @Override
    public boolean isMixin() {
        return true;
    }

    @Override
    protected DataSchemaContextNode<?> fromLocalSchemaAndQName(final DataNodeContainer schema, final QName child) {
        final DataSchemaNode result = findChildSchemaNode(schema, child);
        // We try to look up if this node was added by augmentation
        if (schema instanceof DataSchemaNode && result.isAugmenting()) {
            return fromAugmentation(schema, (AugmentationTarget) schema, result);
        }
        return fromDataSchemaNode(result);
    }

    @Override
    protected Set<QName> getQNameIdentifiers() {
        return getIdentifier().getPossibleChildNames();
    }
}
