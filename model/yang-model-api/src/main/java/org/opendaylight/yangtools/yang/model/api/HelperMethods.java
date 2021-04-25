/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Package-internal helper methods for use in interface default methods.
 *
 * @author Robert Varga
 */
@NonNullByDefault
final class HelperMethods {
    private HelperMethods() {

    }

    static boolean isDataNode(final Optional<DataSchemaNode> optNode) {
        return optNode.isPresent() && isDataNode(optNode.get());
    }

    private static boolean isDataNode(final DataSchemaNode node) {
        return node instanceof ContainerSchemaNode || node instanceof LeafSchemaNode
                || node instanceof LeafListSchemaNode || node instanceof ListSchemaNode
                || node instanceof AnydataSchemaNode || node instanceof AnyxmlSchemaNode;
    }
}
