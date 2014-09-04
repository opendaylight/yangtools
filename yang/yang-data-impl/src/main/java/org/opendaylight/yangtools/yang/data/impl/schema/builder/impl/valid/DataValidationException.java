/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public class DataValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataValidationException(final String message) {
        super(message);
    }

    public static void checkLegalChild(final boolean isLegal, final YangInstanceIdentifier.PathArgument child, final DataNodeContainer schema,
            final Set<QName> childNodes, final Set<YangInstanceIdentifier.AugmentationIdentifier> augments) {
        if (!isLegal) {
            throw new IllegalChildException(child, schema, childNodes, augments);
        }
    }

    public static void checkLegalChild(final boolean isLegal, final YangInstanceIdentifier.PathArgument child, final DataSchemaNode schema,
            final Set<QName> childNodes) {
        if (!isLegal) {
            throw new IllegalChildException(child, schema, childNodes);
        }
    }

    public static void checkLegalChild(final boolean isLegal, final YangInstanceIdentifier.PathArgument child, final ChoiceNode schema) {
        if (!isLegal) {
            throw new IllegalChildException(child, schema);
        }
    }

    public static void checkLegalData(final boolean isLegal, final String messageTemplate, final Object... messageAttrs) {
        if (!isLegal) {
            throw new DataValidationException(String.format(messageTemplate, messageAttrs));
        }
    }

    public static void checkListKey(final DataContainerChild<?, ?> childNode, final Map<QName, Object> keyValues, final QName keyQName,
            final YangInstanceIdentifier.NodeIdentifierWithPredicates nodeId) {
        checkListKey(childNode, keyQName, nodeId);

        //FIXME: parameter keyValues is never used in checkListKey maybe change of method signature?
        final Object expectedValue = nodeId.getKeyValues().get(keyQName);
        final Object actualValue = childNode.getValue();

        Preconditions.checkNotNull(expectedValue);
        Preconditions.checkNotNull(actualValue);
        Preconditions.checkState(actualValue.equals(expectedValue));
    }

    public static void checkListKey(final DataContainerChild<?, ?> childNode, final QName keyQName, final YangInstanceIdentifier.NodeIdentifierWithPredicates nodeId) {
        if (childNode == null) {
            throw new IllegalListKeyException(keyQName, nodeId);
        }
    }

    static final class IllegalChildException extends DataValidationException {
        private static final long serialVersionUID = 1L;

        public IllegalChildException(final YangInstanceIdentifier.PathArgument child, final DataNodeContainer schema,
                final Set<QName> childNodes, final Set<YangInstanceIdentifier.AugmentationIdentifier> augments) {
            super(String.format("Unknown child node: %s, does not belong to: %s as a direct child. "
                    + "Direct child nodes: %s, augmented child nodes: %s", child, schema, childNodes, augments));
        }

        public IllegalChildException(final YangInstanceIdentifier.PathArgument child, final ChoiceNode schema) {
            super(String.format("Unknown child node: %s, not detected in choice: %s", child, schema));
        }

        public IllegalChildException(final YangInstanceIdentifier.PathArgument child, final DataSchemaNode schema, final Set<QName> childNodes) {
            super(String.format("Unknown child node: %s, does not belong to: %s as a child. "
                    + "Child nodes: %s", child, schema, childNodes));
        }
    }

    static final class IllegalListKeyException extends DataValidationException {
        private static final long serialVersionUID = 1L;

        public IllegalListKeyException(final QName keyQName, final YangInstanceIdentifier.NodeIdentifierWithPredicates id) {
            super(String.format("Key value not present for key: %s, in: %s", keyQName, id));
        }

        public IllegalListKeyException(final QName keyQName, final YangInstanceIdentifier.NodeIdentifierWithPredicates id, final Object actualValue, final Object expectedValue) {
            super(String.format("Illegal value for key: %s, in: %s, actual value: %s, expected value from key: %s", keyQName, id, actualValue, expectedValue));
        }
    }
}
