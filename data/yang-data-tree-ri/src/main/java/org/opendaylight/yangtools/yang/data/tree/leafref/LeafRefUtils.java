/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import com.google.common.collect.ImmutableList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

public final class LeafRefUtils {
    private LeafRefUtils() {
        // Hidden on purpose
    }

    /**
     * Create an absolute leafref path.
     *
     * @param leafRefPath leafRefPath
     * @param contextNodeSchemaPath contextNodeSchemaPath
     * @param module module
     * @return LeafRefPath object
     */
    public static LeafRefPath createAbsoluteLeafRefPath(final LeafRefPath leafRefPath,
            final ImmutableList<QName> contextNodeSchemaPath, final Module module) {
        if (leafRefPath.isAbsolute()) {
            return leafRefPath;
        }

        final Deque<QNameWithPredicate> absoluteLeafRefTargetPathList = schemaPathToXPathQNames(
                contextNodeSchemaPath, module);
        for (QNameWithPredicate qname : leafRefPath.getPathFromRoot()) {
            if (qname.equals(QNameWithPredicate.UP_PARENT)) {
                absoluteLeafRefTargetPathList.removeLast();
            } else {
                absoluteLeafRefTargetPathList.add(qname);
            }
        }

        return LeafRefPath.create(absoluteLeafRefTargetPathList, true);
    }

    private static Deque<QNameWithPredicate> schemaPathToXPathQNames(final ImmutableList<QName> nodePath,
            final Module module) {
        final Deque<QNameWithPredicate> xpath = new LinkedList<>();
        final Iterator<QName> nodePathIterator = nodePath.iterator();

        DataNodeContainer currenDataNodeContainer = module;
        while (nodePathIterator.hasNext()) {
            final QName qname = nodePathIterator.next();
            final DataSchemaNode child = currenDataNodeContainer.dataChildByName(qname);

            if (child instanceof DataNodeContainer container) {
                if (!(child instanceof CaseSchemaNode)) {
                    xpath.add(new SimpleQNameWithPredicate(qname));
                }
                currenDataNodeContainer = container;
            } else if (child instanceof ChoiceSchemaNode choice) {
                if (nodePathIterator.hasNext()) {
                    currenDataNodeContainer = choice.findCaseNode(nodePathIterator.next()).orElse(null);
                } else {
                    break;
                }
            } else if (child instanceof LeafSchemaNode || child instanceof LeafListSchemaNode) {
                xpath.add(new SimpleQNameWithPredicate(qname));
                break;
            } else if (child == null) {
                throw new IllegalArgumentException("No child " + qname + " found in node container "
                        + currenDataNodeContainer + " in module " + module.getName());
            } else {
                throw new IllegalStateException("Illegal schema node type in the path: " + child.getClass());
            }
        }

        return xpath;
    }

    public static LeafRefPath schemaPathToLeafRefPath(final ImmutableList<QName> nodePath, final Module module) {
        return LeafRefPath.create(schemaPathToXPathQNames(nodePath, module), true);
    }
}
