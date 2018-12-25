/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * General validator for container like statements, e.g. container, list-entry, choice, augment
 */
public class DataNodeContainerValidator {
    private final Set<AugmentationIdentifier> augments = new HashSet<>();
    private final DataNodeContainer schema;
    private final Set<QName> childNodes;

    public DataNodeContainerValidator(final DataNodeContainer schema) {
        this.schema = requireNonNull(schema, "Schema was null");
        this.childNodes = getChildNodes(schema);

        if (schema instanceof AugmentationTarget) {
            for (AugmentationSchemaNode augmentation : ((AugmentationTarget) schema).getAvailableAugmentations()) {
                augments.add(DataSchemaContextNode.augmentationIdentifierFrom(augmentation));
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
     * Map all direct child nodes. Skip augments since they have no qname. List cases since cases do not exist in
     * NormalizedNode API.
     */
    private static Set<QName> getChildNodes(final DataNodeContainer nodeContainer) {
        Set<QName> allChildNodes = new HashSet<>();

        for (DataSchemaNode childSchema : nodeContainer.getChildNodes()) {
            if (childSchema instanceof CaseSchemaNode) {
                allChildNodes.addAll(getChildNodes((DataNodeContainer) childSchema));
            } else if (!(childSchema instanceof AugmentationSchemaNode)) {
                allChildNodes.add(childSchema.getQName());
            }
        }

        return allChildNodes;
    }
}
