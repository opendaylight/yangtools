/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid;

import com.google.common.annotations.Beta;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public class DataValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataValidationException(final String message) {
        super(message);
    }

    public static void checkLegalChild(final boolean isLegal, final PathArgument child, final DataNodeContainer schema,
            final Set<QName> childNodes, final Set<AugmentationIdentifier> augments) {
        if (!isLegal) {
            throw new IllegalChildException(child, schema, childNodes, augments);
        }
    }

    public static void checkLegalChild(final boolean isLegal, final PathArgument child, final DataSchemaNode schema,
            final Set<QName> childNodes) {
        if (!isLegal) {
            throw new IllegalChildException(child, schema, childNodes);
        }
    }

    public static void checkLegalChild(final boolean isLegal, final PathArgument child, final ChoiceSchemaNode schema) {
        if (!isLegal) {
            throw new IllegalChildException(child, schema);
        }
    }

    public static void checkLegalData(final boolean isLegal, final String messageTemplate,
            final Object... messageAttrs) {
        if (!isLegal) {
            throw new DataValidationException(String.format(messageTemplate, messageAttrs));
        }
    }

    public static void checkListKey(final NodeIdentifierWithPredicates nodeId, final QName keyQName,
            final Object expected, final Object actual) {
        // Objects.equals() does not deal with arrays, but is faster
        if (!Objects.equals(expected, actual) && !Objects.deepEquals(expected, actual)) {
            throw new IllegalListKeyException(keyQName, nodeId, actual, expected);
        }
    }

    public static void checkListKey(final DataContainerChild<?, ?> childNode, final Map<QName, Object> keyValues,
            final QName keyQName, final NodeIdentifierWithPredicates nodeId) {
        checkListKey(childNode, keyQName, nodeId);

        final Object expected = keyValues.get(keyQName);
        final Object actual = childNode.getValue();

        checkListKey(nodeId, keyQName, expected, actual);
    }

    public static void checkListKey(final DataContainerChild<?, ?> childNode, final QName keyQName,
            final NodeIdentifierWithPredicates nodeId) {
        if (childNode == null) {
            throw new IllegalListKeyException(keyQName, nodeId);
        }
    }

    private static final class IllegalChildException extends DataValidationException {
        private static final long serialVersionUID = 1L;

        IllegalChildException(final PathArgument child, final DataNodeContainer schema,
                final Set<QName> childNodes, final Set<AugmentationIdentifier> augments) {
            super(String.format("Unknown child node: %s, does not belong to: %s as a direct child. "
                    + "Direct child nodes: %s, augmented child nodes: %s", child, schema, childNodes, augments));
        }

        IllegalChildException(final PathArgument child, final ChoiceSchemaNode schema) {
            super(String.format("Unknown child node: %s, not detected in choice: %s", child, schema));
        }

        IllegalChildException(final PathArgument child, final DataSchemaNode schema,
                final Set<QName> childNodes) {
            super(String.format("Unknown child node: %s, does not belong to: %s as a child. Child nodes: %s", child,
                schema, childNodes));
        }
    }

    @Beta
    public static final class IllegalListKeyException extends DataValidationException {
        private static final long serialVersionUID = 1L;

        public IllegalListKeyException(final String format, final Object... args) {
            super(String.format(format, args));
        }

        IllegalListKeyException(final QName keyQName, final NodeIdentifierWithPredicates id) {
            this("Key value not present for key: %s, in: %s", keyQName, id);
        }

        IllegalListKeyException(final QName keyQName, final NodeIdentifierWithPredicates id, final Object actualValue,
                final Object expectedValue) {
            this("Illegal value for key: %s, in: %s, actual value: %s, expected value from key: %s",
                keyQName, id, actualValue, expectedValue);
        }
    }
}
