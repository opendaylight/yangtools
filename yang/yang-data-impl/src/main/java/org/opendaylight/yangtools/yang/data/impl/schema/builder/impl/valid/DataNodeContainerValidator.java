/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * General validator for container like statements, e.g. container, list-entry, choice, augment
 */
public class DataNodeContainerValidator {

    private final DataNodeContainer schema;
    private final Set<QName> childNodes;
    private final Set<AugmentationIdentifier> augments = new HashSet<>();

    public DataNodeContainerValidator(final DataNodeContainer schema) {
        this.schema = Preconditions.checkNotNull(schema, "Schema was null");

        this.childNodes = getChildNodes(schema);

        if(schema instanceof AugmentationTarget) {
            for (AugmentationSchema augmentationSchema : ((AugmentationTarget) schema).getAvailableAugmentations()) {
                augments.add(SchemaUtils.getNodeIdentifierForAugmentation(augmentationSchema));
            }
        }
    }

    private boolean isKnownChild(final PathArgument child) {
        if (child instanceof AugmentationIdentifier) {
            return augments.contains(child);
        }

        return childNodes.contains(child.getNodeType());
    }

    public void validateChild(final PathArgument child) {
        DataValidationException.checkLegalChild(isKnownChild(child), child, schema, childNodes, augments);
    }

    public DataContainerChild<?, ?> validateChild(final DataContainerChild<?, ?> child) {
        validateChild(child.getIdentifier());
        return child;
    }

    /**
     * Map all direct child nodes. Skip augments since they have no qname. List cases since cases do not exist in NormalizedNode API.
     */
    private static Set<QName> getChildNodes(final DataNodeContainer nodeContainer) {
        Set<QName> allChildNodes = Sets.newHashSet();

        for (DataSchemaNode childSchema : nodeContainer.getChildNodes()) {
            if(childSchema instanceof ChoiceCaseNode) {
                allChildNodes.addAll(getChildNodes((DataNodeContainer) childSchema));
            } else if (!(childSchema instanceof AugmentationSchema)) {
                allChildNodes.add(childSchema.getQName());
            }
        }

        return allChildNodes;
    }

}
