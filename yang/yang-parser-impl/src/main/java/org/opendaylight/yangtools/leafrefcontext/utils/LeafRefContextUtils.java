/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext.utils;

import org.opendaylight.yangtools.leafrefcontext.api.LeafRefContext;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

public class LeafRefContextUtils {


    public static LeafRefContext getLeafRefReferencingContext(SchemaNode node, LeafRefContext root){
        SchemaPath schemaPath = node.getPath();
        return getLeafRefReferencingContext(schemaPath,root);
    }

    public static LeafRefContext getLeafRefReferencingContext(
            SchemaPath schemaPath, LeafRefContext root) {
        Iterable<QName> pathFromRoot = schemaPath.getPathFromRoot();
        return getLeafRefReferencingContext(pathFromRoot,root);
    }

    public static LeafRefContext getLeafRefReferencingContext(
            Iterable<QName> pathFromRoot, LeafRefContext root) {

        LeafRefContext leafRefCtx = null;
        Iterator<QName> iterator = pathFromRoot.iterator();
        while(iterator.hasNext() && root !=null){
            QName qname = iterator.next();
            leafRefCtx = root.getReferencingChildByName(qname);
            if(iterator.hasNext()) {
                root = leafRefCtx;
            }
        }

        return leafRefCtx;
    }


    public static LeafRefContext getLeafRefReferencedByContext(SchemaNode node, LeafRefContext root){
        SchemaPath schemaPath = node.getPath();
        return getLeafRefReferencedByContext(schemaPath,root);
    }

    public static LeafRefContext getLeafRefReferencedByContext(
            SchemaPath schemaPath, LeafRefContext root) {
        Iterable<QName> pathFromRoot = schemaPath.getPathFromRoot();
        return getLeafRefReferencedByContext(pathFromRoot,root);
    }

    public static LeafRefContext getLeafRefReferencedByContext(
            Iterable<QName> pathFromRoot, LeafRefContext root) {

        LeafRefContext leafRefCtx = null;
        Iterator<QName> iterator = pathFromRoot.iterator();
        while(iterator.hasNext() && root !=null){
            QName qname = iterator.next();
            leafRefCtx = root.getReferencedByChildByName(qname);
            if(iterator.hasNext()) {
                root = leafRefCtx;
            }
        }

        return leafRefCtx;
    }


    public static boolean isLeafRef(SchemaNode node, LeafRefContext root) {
        LeafRefContext leafRefReferencingContext = getLeafRefReferencingContext(node, root);
        return leafRefReferencingContext.isReferencing();
    }

    public static boolean hasLeafRefChild(SchemaNode node, LeafRefContext root) {
        LeafRefContext leafRefReferencingContext = getLeafRefReferencingContext(node, root);
        return leafRefReferencingContext.hasReferencingChild();
    }

    public static boolean isReferencedByLeafRef(SchemaNode node, LeafRefContext root) {
        LeafRefContext leafRefReferencedByContext = getLeafRefReferencedByContext(node, root);
        return leafRefReferencedByContext.isReferencedBy();
    }

    public static boolean hasChildReferencedByLeafRef(SchemaNode node, LeafRefContext root) {
        LeafRefContext leafRefReferencedByContext = getLeafRefReferencedByContext(node, root);
        return leafRefReferencedByContext.hasReferencedByChild();
    }

    public static List<LeafRefContext> findAllLeafRefChilds(SchemaNode node,
            LeafRefContext root) {

        return findAllLeafRefChilds(node.getPath(),root);
    }

    public static List<LeafRefContext> findAllLeafRefChilds(SchemaPath schemaPath,
            LeafRefContext root) {

       return findAllLeafRefChilds(schemaPath.getPathFromRoot(),root);
    }


    public static List<LeafRefContext> findAllLeafRefChilds(Iterable<QName> pathFromRoot,
            LeafRefContext root) {

        LeafRefContext leafRefReferencingContext = getLeafRefReferencingContext(
                pathFromRoot, root);
        List<LeafRefContext> allLeafRefsChilds = findAllLeafRefChilds(leafRefReferencingContext);

        return allLeafRefsChilds;
    }


    public static List<LeafRefContext> findAllLeafRefChilds(LeafRefContext parent){

        LinkedList<LeafRefContext> leafRefChilds = new LinkedList<LeafRefContext>();

        if(parent == null) {
            return leafRefChilds;
        }

        if(parent.isReferencing()) {
            leafRefChilds.add(parent);
            return leafRefChilds;
        } else {
            Set<Entry<QName, LeafRefContext>> childs = parent.getReferencingChilds().entrySet();
            for (Entry<QName, LeafRefContext> child : childs) {
                leafRefChilds.addAll(findAllLeafRefChilds(child.getValue()));
            }
        }
        return leafRefChilds;
    }


    public static List<LeafRefContext> findAllChildsReferencedByLeafRef(SchemaNode node,
            LeafRefContext root) {

        return findAllChildsReferencedByLeafRef(node.getPath(),root);
    }

    public static List<LeafRefContext> findAllChildsReferencedByLeafRef(SchemaPath schemaPath,
            LeafRefContext root) {

       return findAllChildsReferencedByLeafRef(schemaPath.getPathFromRoot(),root);
    }


    public static List<LeafRefContext> findAllChildsReferencedByLeafRef(Iterable<QName> pathFromRoot,
            LeafRefContext root) {

        LeafRefContext leafRefReferencedByContext = getLeafRefReferencedByContext(
                pathFromRoot, root);
        List<LeafRefContext> allChildsReferencedByLeafRef = findAllChildsReferencedByLeafRef(leafRefReferencedByContext);

        return allChildsReferencedByLeafRef;
    }


    public static List<LeafRefContext> findAllChildsReferencedByLeafRef(LeafRefContext parent){

        LinkedList<LeafRefContext> childsReferencedByLeafRef = new LinkedList<LeafRefContext>();

        if(parent == null) {
            return childsReferencedByLeafRef;
        }

        if(parent.isReferencedBy()) {
            childsReferencedByLeafRef.add(parent);
            return childsReferencedByLeafRef;
        } else {
            Set<Entry<QName, LeafRefContext>> childs = parent.getReferencedByChilds().entrySet();
            for (Entry<QName, LeafRefContext> child : childs) {
                childsReferencedByLeafRef.addAll(findAllChildsReferencedByLeafRef(child.getValue()));
            }
        }
        return childsReferencedByLeafRef;
    }

    public static Map<QName, LeafRefContext> getAllLeafRefsReferencingThisNode(SchemaNode node,LeafRefContext root){
        return getAllLeafRefsReferencingThisNode(node.getPath(),root);
    }

    public static Map<QName, LeafRefContext> getAllLeafRefsReferencingThisNode(SchemaPath path, LeafRefContext root){
        return getAllLeafRefsReferencingThisNode(path.getPathFromRoot(),root);
    }

    public static Map<QName, LeafRefContext> getAllLeafRefsReferencingThisNode(Iterable<QName> pathFromRoot, LeafRefContext root){
        LeafRefContext leafRefReferencedByContext = getLeafRefReferencedByContext(pathFromRoot, root);
        return leafRefReferencedByContext.getAllReferencedByLeafRefCtxs();
    }


}
