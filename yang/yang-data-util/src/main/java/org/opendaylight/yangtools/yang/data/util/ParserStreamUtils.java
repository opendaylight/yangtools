/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.util;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;

public final class ParserStreamUtils {

    private ParserStreamUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated.");
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
                                                                   final String childName, final URI namespace) {
        final Deque<DataSchemaNode> result = new ArrayDeque<>();
        final List<ChoiceSchemaNode> childChoices = new ArrayList<>();
        DataSchemaNode potentialChildNode = null;

        if (dataSchemaNode instanceof NotificationNodeContainer) {
            for (final NotificationDefinition noti : ((NotificationNodeContainer)dataSchemaNode).getNotifications()) {
                if (noti.getQName().getLocalName().equals(childName)
                      && noti.getQName().getNamespace().equals(namespace)) {
                    if (potentialChildNode == null
                        || noti.getQName().getRevision().after(potentialChildNode.getQName().getRevision())) {
                        potentialChildNode = dataSchemaNode;
                    }
                }
            }
        }
        if (potentialChildNode != null) {
            result.push(potentialChildNode);
            return result;
        }

        if (dataSchemaNode instanceof DataNodeContainer) {
            for (final DataSchemaNode childNode : ((DataNodeContainer) dataSchemaNode).getChildNodes()) {
                if (childNode instanceof ChoiceSchemaNode) {
                    childChoices.add((ChoiceSchemaNode) childNode);
                } else {
                    final QName childQName = childNode.getQName();

                    if (childQName.getLocalName().equals(childName) && childQName.getNamespace().equals(namespace)) {
                        if (potentialChildNode == null
                                || childQName.getRevision().after(potentialChildNode.getQName().getRevision())) {
                            potentialChildNode = childNode;
                        }
                    }
                }
            }
        }
        if (potentialChildNode != null) {
            result.push(potentialChildNode);
            return result;
        }

        // try to find data schema node in choice (looking for first match)
        for (final ChoiceSchemaNode choiceNode : childChoices) {
            for (final ChoiceCaseNode concreteCase : choiceNode.getCases()) {
                final Deque<DataSchemaNode> resultFromRecursion = findSchemaNodeByNameAndNamespace(concreteCase,
                        childName, namespace);
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
