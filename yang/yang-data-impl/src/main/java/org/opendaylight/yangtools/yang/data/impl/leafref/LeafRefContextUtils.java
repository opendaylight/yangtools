/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class LeafRefContextUtils {

    private LeafRefContextUtils() {
        throw new UnsupportedOperationException();
    }

    public static LeafRefContext getLeafRefReferencingContext(final SchemaNode node, final LeafRefContext root) {
        final SchemaPath schemaPath = node.getPath();
        return getLeafRefReferencingContext(schemaPath, root);
    }

    public static LeafRefContext getLeafRefReferencingContext(
            final SchemaPath schemaPath, final LeafRefContext root) {
        final Iterable<QName> pathFromRoot = schemaPath.getPathFromRoot();
        return getLeafRefReferencingContext(pathFromRoot, root);
    }

    public static LeafRefContext getLeafRefReferencingContext(final Iterable<QName> pathFromRoot, LeafRefContext root) {
        LeafRefContext leafRefCtx = null;
        final Iterator<QName> iterator = pathFromRoot.iterator();
        while (iterator.hasNext() && root != null) {
            final QName qname = iterator.next();
            leafRefCtx = root.getReferencingChildByName(qname);
            if (iterator.hasNext()) {
                root = leafRefCtx;
            }
        }

        return leafRefCtx;
    }

    public static LeafRefContext getLeafRefReferencedByContext(final SchemaNode node, final LeafRefContext root) {
        final SchemaPath schemaPath = node.getPath();
        return getLeafRefReferencedByContext(schemaPath, root);
    }

    public static LeafRefContext getLeafRefReferencedByContext(
            final SchemaPath schemaPath, final LeafRefContext root) {
        final Iterable<QName> pathFromRoot = schemaPath.getPathFromRoot();
        return getLeafRefReferencedByContext(pathFromRoot, root);
    }

    public static LeafRefContext getLeafRefReferencedByContext(final Iterable<QName> pathFromRoot,
            LeafRefContext root) {

        LeafRefContext leafRefCtx = null;
        final Iterator<QName> iterator = pathFromRoot.iterator();
        while (iterator.hasNext() && root != null) {
            final QName qname = iterator.next();
            leafRefCtx = root.getReferencedChildByName(qname);
            if (iterator.hasNext()) {
                root = leafRefCtx;
            }
        }

        return leafRefCtx;
    }

    public static boolean isLeafRef(final SchemaNode node, final LeafRefContext root) {
        if (node == null || root == null) {
            return false;
        }

        final LeafRefContext leafRefReferencingContext = getLeafRefReferencingContext(node, root);
        if (leafRefReferencingContext == null) {
            return false;
        }

        return leafRefReferencingContext.isReferencing();
    }

    public static boolean hasLeafRefChild(final SchemaNode node, final LeafRefContext root) {
        if (node == null || root == null) {
            return false;
        }

        final LeafRefContext leafRefReferencingContext = getLeafRefReferencingContext(node, root);
        if (leafRefReferencingContext == null) {
            return false;
        }

        return leafRefReferencingContext.hasReferencingChild();
    }

    public static boolean isReferencedByLeafRef(final SchemaNode node, final LeafRefContext root) {
        if (node == null || root == null) {
            return false;
        }

        final LeafRefContext leafRefReferencedByContext = getLeafRefReferencedByContext(node, root);
        if (leafRefReferencedByContext == null) {
            return false;
        }

        return leafRefReferencedByContext.isReferenced();
    }

    public static boolean hasChildReferencedByLeafRef(final SchemaNode node, final LeafRefContext root) {
        if (node == null || root == null) {
            return false;
        }

        final LeafRefContext leafRefReferencedByContext = getLeafRefReferencedByContext(node, root);
        if (leafRefReferencedByContext == null) {
            return false;
        }

        return leafRefReferencedByContext.hasReferencedChild();
    }

    public static List<LeafRefContext> findAllLeafRefChilds(final SchemaNode node, final LeafRefContext root) {
        return findAllLeafRefChilds(node.getPath(), root);
    }

    public static List<LeafRefContext> findAllLeafRefChilds(final SchemaPath schemaPath, final LeafRefContext root) {
        return findAllLeafRefChilds(schemaPath.getPathFromRoot(), root);
    }

    public static List<LeafRefContext> findAllLeafRefChilds(final Iterable<QName> pathFromRoot,
            final LeafRefContext root) {
        final LeafRefContext leafRefReferencingContext = getLeafRefReferencingContext(pathFromRoot, root);
        final List<LeafRefContext> allLeafRefsChilds = findAllLeafRefChilds(leafRefReferencingContext);

        return allLeafRefsChilds;
    }

    public static List<LeafRefContext> findAllLeafRefChilds(final LeafRefContext parent) {
        final LinkedList<LeafRefContext> leafRefChilds = new LinkedList<>();
        if (parent == null) {
            return leafRefChilds;
        }

        if (parent.isReferencing()) {
            leafRefChilds.add(parent);
            return leafRefChilds;
        }

        final Set<Entry<QName, LeafRefContext>> childs = parent.getReferencingChilds().entrySet();
        for (final Entry<QName, LeafRefContext> child : childs) {
            leafRefChilds.addAll(findAllLeafRefChilds(child.getValue()));
        }
        return leafRefChilds;
    }

    public static List<LeafRefContext> findAllChildsReferencedByLeafRef(final SchemaNode node,
            final LeafRefContext root) {
        return findAllChildsReferencedByLeafRef(node.getPath(), root);
    }

    public static List<LeafRefContext> findAllChildsReferencedByLeafRef(final SchemaPath schemaPath,
            final LeafRefContext root) {
        return findAllChildsReferencedByLeafRef(schemaPath.getPathFromRoot(), root);
    }

    public static List<LeafRefContext> findAllChildsReferencedByLeafRef(final Iterable<QName> pathFromRoot,
            final LeafRefContext root) {

        final LeafRefContext leafRefReferencedByContext = getLeafRefReferencedByContext(pathFromRoot, root);
        final List<LeafRefContext> allChildsReferencedByLeafRef =
                findAllChildsReferencedByLeafRef(leafRefReferencedByContext);

        return allChildsReferencedByLeafRef;
    }

    public static List<LeafRefContext> findAllChildsReferencedByLeafRef(final LeafRefContext parent) {
        final LinkedList<LeafRefContext> childsReferencedByLeafRef = new LinkedList<>();
        if (parent == null) {
            return childsReferencedByLeafRef;
        }

        if (parent.isReferenced()) {
            childsReferencedByLeafRef.add(parent);
            return childsReferencedByLeafRef;
        }

        final Set<Entry<QName, LeafRefContext>> childs = parent.getReferencedByChilds().entrySet();
        for (final Entry<QName, LeafRefContext> child : childs) {
            childsReferencedByLeafRef.addAll(findAllChildsReferencedByLeafRef(child.getValue()));
        }
        return childsReferencedByLeafRef;
    }

    public static Map<QName, LeafRefContext> getAllLeafRefsReferencingThisNode(
            final SchemaNode node, final LeafRefContext root) {
        return getAllLeafRefsReferencingThisNode(node.getPath(), root);
    }

    public static Map<QName, LeafRefContext> getAllLeafRefsReferencingThisNode(final SchemaPath path,
            final LeafRefContext root) {
        return getAllLeafRefsReferencingThisNode(path.getPathFromRoot(), root);
    }

    public static Map<QName, LeafRefContext> getAllLeafRefsReferencingThisNode(final Iterable<QName> pathFromRoot,
            final LeafRefContext root) {

        final LeafRefContext leafRefReferencedByContext = getLeafRefReferencedByContext(pathFromRoot, root);
        if (leafRefReferencedByContext == null) {
            return new HashMap<>();
        }

        return leafRefReferencedByContext.getAllReferencedByLeafRefCtxs();
    }
}
