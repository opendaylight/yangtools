/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

import java.util.Map;
import java.util.Set;

public class DataValidationException extends RuntimeException {

    public DataValidationException(String message) {
        super(message);
    }

    public static void checkLegalChild(boolean isLegal, InstanceIdentifier.PathArgument child, DataNodeContainer schema,
            Set<QName> childNodes, Set<InstanceIdentifier.AugmentationIdentifier> augments) {
        if (isLegal == false) {
            throw new IllegalChildException(child, schema, childNodes, augments);
        }
    }

    public static void checkLegalChild(boolean isLegal, InstanceIdentifier.PathArgument child, DataSchemaNode schema,
            Set<QName> childNodes) {
        if (isLegal == false) {
            throw new IllegalChildException(child, schema, childNodes);
        }
    }

    public static void checkLegalChild(boolean isLegal, InstanceIdentifier.PathArgument child, ChoiceNode schema) {
        if (isLegal == false) {
            throw new IllegalChildException(child, schema);
        }
    }

    public static void checkLegalData(boolean isLegal, String messageTemplate, Object... messageAttrs) {
        if (isLegal == false) {
            throw new DataValidationException(String.format(messageTemplate, messageAttrs));
        }
    }

    public static void checkListKey(DataContainerChild<?, ?> childNode, Map<QName, Object> keyValues, QName keyQName,
            InstanceIdentifier.NodeIdentifierWithPredicates nodeId) {
        checkListKey(childNode, keyQName, nodeId);

        Object expectedValue = nodeId.getKeyValues().get(keyQName);
        Object actualValue = childNode.getValue();
        if (childNode == null) {
            throw new IllegalListKeyException(keyQName, nodeId, actualValue, expectedValue);
        }
    }

    public static void checkListKey(DataContainerChild<?, ?> childNode, QName keyQName, InstanceIdentifier.NodeIdentifierWithPredicates nodeId) {
        if (childNode == null) {
            throw new IllegalListKeyException(keyQName, nodeId);
        }
    }

    static final class IllegalChildException extends DataValidationException {

        public IllegalChildException(InstanceIdentifier.PathArgument child, DataNodeContainer schema,
                Set<QName> childNodes, Set<InstanceIdentifier.AugmentationIdentifier> augments) {
            super(String.format("Unknown child node: %s, does not belong to: %s as a direct child. "
                    + "Direct child nodes: %s, augmented child nodes: %s", child, schema, childNodes, augments));
        }

        public IllegalChildException(InstanceIdentifier.PathArgument child, ChoiceNode schema) {
            super(String.format("Unknown child node: %s, not detected in choice: %s", child, schema));
        }

        public IllegalChildException(InstanceIdentifier.PathArgument child, DataSchemaNode schema, Set<QName> childNodes) {
            super(String.format("Unknown child node: %s, does not belong to: %s as a child. "
                    + "Child nodes: %s", child, schema, childNodes));
        }
    }

    static final class IllegalListKeyException extends DataValidationException {

        public IllegalListKeyException(QName keyQName, InstanceIdentifier.NodeIdentifierWithPredicates id) {
            super(String.format("Key value not present for key: %s, in: %s", keyQName, id));
        }

        public IllegalListKeyException(QName keyQName, InstanceIdentifier.NodeIdentifierWithPredicates id, Object actualValue, Object expectedValue) {
            super(String.format("Illegal value for key: %s, in: %s, actual value: %s, expected value from key: %s", keyQName, id, actualValue, expectedValue));
        }
    }
}
