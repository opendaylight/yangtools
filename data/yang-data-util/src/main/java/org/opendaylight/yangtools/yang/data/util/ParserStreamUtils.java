/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;

public final class ParserStreamUtils {
    private ParserStreamUtils() {
        // Hidden on purpose
    }

    /**
     * Returns stack of schema nodes via which it was necessary to pass to get schema node with specified
     * {@code childName} and {@code namespace}.
     *
     * @return stack of schema nodes via which it was passed through. If found schema node is direct child then stack
     *         contains only one node. If it is found under choice and case then stack should contains 2*n+1 element
     *         (where n is number of choices through it was passed)
     */
    public static Deque<DataSchemaNode> findSchemaNodeByNameAndNamespace(final DataSchemaNode dataSchemaNode,
            final String childName, final XMLNamespace namespace) {
        final var result = new ArrayDeque<DataSchemaNode>();
        final var childChoices = new ArrayList<ChoiceSchemaNode>();
        DataSchemaNode potentialChildNode = null;
        if (dataSchemaNode instanceof DataNodeContainer dataContainer) {
            for (final var childNode : dataContainer.getChildNodes()) {
                if (childNode instanceof ChoiceSchemaNode choice) {
                    childChoices.add(choice);
                } else {
                    final var childQName = childNode.getQName();
                    if (childQName.getLocalName().equals(childName) && childQName.getNamespace().equals(namespace)
                            && (potentialChildNode == null || Revision.compare(childQName.getRevision(),
                                potentialChildNode.getQName().getRevision()) > 0)) {
                        potentialChildNode = childNode;
                    }
                }
            }
        }
        if (potentialChildNode == null && dataSchemaNode instanceof NotificationDefinition notification
                && notification.getQName().getLocalName().equals(childName)
                && notification.getQName().getNamespace().equals(namespace)) {
            potentialChildNode = dataSchemaNode;
        }
        if (potentialChildNode != null) {
            result.push(potentialChildNode);
            return result;
        }

        // try to find data schema node in choice (looking for first match)
        for (final var choiceNode : childChoices) {
            for (final var concreteCase : choiceNode.getCases()) {
                final var resultFromRecursion = findSchemaNodeByNameAndNamespace(concreteCase, childName, namespace);
                if (!resultFromRecursion.isEmpty()) {
                    resultFromRecursion.push(concreteCase);
                    resultFromRecursion.push(choiceNode);
                    return resultFromRecursion;
                }
            }
        }
        return result;
    }
}
