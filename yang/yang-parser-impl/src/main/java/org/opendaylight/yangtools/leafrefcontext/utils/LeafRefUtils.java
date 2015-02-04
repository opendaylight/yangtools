/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext.utils;

import org.opendaylight.yangtools.leafrefcontext.api.LeafRefPath;

import org.opendaylight.yangtools.leafrefcontext.builder.QNameWithPredicateBuilder;
import org.opendaylight.yangtools.leafrefcontext.api.QNameWithPredicate;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import java.util.LinkedList;
import java.util.Iterator;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class LeafRefUtils {



    /**
     * @param leafRefPath
     * @param contextNodeSchemaPath
     * @return
     */
    public static LeafRefPath createAbsoluteLeafRefPath(
            LeafRefPath leafRefPath, SchemaPath contextNodeSchemaPath, Module module) {

        if(leafRefPath.isAbsolute()) return leafRefPath;

        LinkedList<QNameWithPredicate> absoluteLeafRefTargetPathList = schemaPathToXPathQNames(contextNodeSchemaPath, module);

        Iterable<QNameWithPredicate> leafRefTargetPathFromRoot = leafRefPath.getPathFromRoot();
        Iterator<QNameWithPredicate> leafRefTgtPathFromRootIterator = leafRefTargetPathFromRoot.iterator();


        while(leafRefTgtPathFromRootIterator.hasNext()) {
            QNameWithPredicate qname = leafRefTgtPathFromRootIterator.next();
            if(qname.equals(QNameWithPredicate.UP_PARENT)) {
                absoluteLeafRefTargetPathList.removeLast();
            } else {
                absoluteLeafRefTargetPathList.add(qname);
            }
        }

        return LeafRefPath.create(absoluteLeafRefTargetPathList, true);
    }

    /**
     * @param currentNodePath
     * @param module
     * @param absoluteLeafRefTargetPathList
     */
    private static LinkedList<QNameWithPredicate> schemaPathToXPathQNames(
            SchemaPath nodePath, Module module) {

        LinkedList<QNameWithPredicate> xpath = new LinkedList<QNameWithPredicate>();

        Iterator<QName> nodePathIterator = nodePath.getPathFromRoot()
                .iterator();

        DataNodeContainer currenDataNodeContainer = module;
        while (nodePathIterator.hasNext()) {
            QName qname = nodePathIterator.next();
            DataSchemaNode child = currenDataNodeContainer
                    .getDataChildByName(qname);

            if (child instanceof DataNodeContainer) {
                if (!(child instanceof ChoiceCaseNode)) {
                    QNameWithPredicate newQName = new QNameWithPredicateBuilder(
                            qname.getModule(), qname.getLocalName()).build();
                    xpath.add(newQName);
                }
                currenDataNodeContainer = (DataNodeContainer) child;
            } else if(child instanceof ChoiceNode){
                if (nodePathIterator.hasNext()) {
                    currenDataNodeContainer = ((ChoiceNode) child)
                            .getCaseNodeByName(nodePathIterator.next());
                } else {
                    break;
                }
            } else if (child instanceof LeafSchemaNode
                    || child instanceof LeafListSchemaNode) {

                QNameWithPredicate newQName = new QNameWithPredicateBuilder(
                        qname.getModule(), qname.getLocalName()).build();
                xpath.add(newQName);
                break;

            } else if (child == null) {
                throw new IllegalArgumentException("No child " + qname
                        + " found in node container " + currenDataNodeContainer
                        + " in module " + module.getName());
            } else {
                throw new IllegalStateException(
                        "Illegal schema node type in the path: "
                                + child.getClass());
            }
        }

        return xpath;
    }

    public static LeafRefPath schemaPathToLeafRefPath(SchemaPath nodePath, Module module){
        LinkedList<QNameWithPredicate> xpathQNames = schemaPathToXPathQNames(nodePath, module);
        return LeafRefPath.create(xpathQNames, true);
    }

}
