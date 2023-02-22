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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;

/**
 * General validator for container like statements, e.g. container, list-entry, choice, augment
 */
public class DataNodeContainerValidator {
    private final DataNodeContainer schema;
    private final Set<QName> childNodes;

    public DataNodeContainerValidator(final DataNodeContainer schema) {
        this.schema = requireNonNull(schema, "Schema was null");
        childNodes = getChildNodes(schema);
    }

    private boolean isKnownChild(final PathArgument child) {
        return childNodes.contains(child.getNodeType());
    }

    public void validateChild(final PathArgument child) {
        DataValidationException.checkLegalChild(isKnownChild(child), child, schema, childNodes);
    }

    public DataContainerChild validateChild(final DataContainerChild child) {
        validateChild(child.getIdentifier());
        return child;
    }

    /**
     * Map all direct child nodes. Skip augments since they have no qname. List cases since cases do not exist in
     * NormalizedNode API.
     */
    private static Set<QName> getChildNodes(final DataNodeContainer nodeContainer) {
        Set<QName> allChildNodes = new HashSet<>();

        for (var childSchema : nodeContainer.getChildNodes()) {
            if (childSchema instanceof CaseSchemaNode caseChildSchema) {
                allChildNodes.addAll(getChildNodes(caseChildSchema));
            } else if (!(childSchema instanceof AugmentationSchemaNode)) {
                allChildNodes.add(childSchema.getQName());
            }
        }

        return allChildNodes;
    }
}
