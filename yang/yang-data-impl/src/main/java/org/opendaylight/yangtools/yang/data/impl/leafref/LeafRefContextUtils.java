/*
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
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

public final class LeafRefContextUtils {
    private LeafRefContextUtils() {
        // Hidden on purpose
    }

    public static LeafRefContext getLeafRefReferencingContext(final SchemaInferenceStack node,
            final LeafRefContext root) {
        final Iterator<QName> iterator = node.schemaPathIterator();
        LeafRefContext leafRefCtx = null;
        LeafRefContext current = root;
        while (iterator.hasNext() && current != null) {
            final QName qname = iterator.next();
            leafRefCtx = current.getReferencingChildByName(qname);
            if (iterator.hasNext()) {
                current = leafRefCtx;
            }
        }

        return leafRefCtx;
    }

    public static LeafRefContext getLeafRefReferencedByContext(final SchemaInferenceStack node,
            final LeafRefContext root) {
        final Iterator<QName> iterator = node.schemaPathIterator();
        LeafRefContext leafRefCtx = null;
        LeafRefContext current = root;
        while (iterator.hasNext() && current != null) {
            final QName qname = iterator.next();
            leafRefCtx = current.getReferencedChildByName(qname);
            if (iterator.hasNext()) {
                current = leafRefCtx;
            }
        }

        return leafRefCtx;
    }

    public static boolean isLeafRef(final SchemaInferenceStack node, final LeafRefContext root) {
        if (node == null || root == null) {
            return false;
        }

        final LeafRefContext leafRefReferencingContext = getLeafRefReferencingContext(node, root);
        if (leafRefReferencingContext == null) {
            return false;
        }

        return leafRefReferencingContext.isReferencing();
    }

    public static boolean hasLeafRefChild(final SchemaInferenceStack node, final LeafRefContext root) {
        if (node == null || root == null) {
            return false;
        }

        final LeafRefContext leafRefReferencingContext = getLeafRefReferencingContext(node, root);
        if (leafRefReferencingContext == null) {
            return false;
        }

        return leafRefReferencingContext.hasReferencingChild();
    }

    public static boolean isReferencedByLeafRef(final SchemaInferenceStack node, final LeafRefContext root) {
        if (node == null || root == null) {
            return false;
        }

        final LeafRefContext leafRefReferencedByContext = getLeafRefReferencedByContext(node, root);
        if (leafRefReferencedByContext == null) {
            return false;
        }

        return leafRefReferencedByContext.isReferenced();
    }

    public static boolean hasChildReferencedByLeafRef(final SchemaInferenceStack node, final LeafRefContext root) {
        if (node == null || root == null) {
            return false;
        }

        final LeafRefContext leafRefReferencedByContext = getLeafRefReferencedByContext(node, root);
        if (leafRefReferencedByContext == null) {
            return false;
        }

        return leafRefReferencedByContext.hasReferencedChild();
    }

    public static List<LeafRefContext> findAllLeafRefChilds(final SchemaInferenceStack node,
            final LeafRefContext root) {
        return findAllLeafRefChilds(getLeafRefReferencingContext(node, root));
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

    public static List<LeafRefContext> findAllChildsReferencedByLeafRef(final SchemaInferenceStack node,
            final LeafRefContext root) {
        return findAllChildsReferencedByLeafRef(getLeafRefReferencedByContext(node, root));
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

    public static Map<QName, LeafRefContext> getAllLeafRefsReferencingThisNode(final SchemaInferenceStack node,
            final LeafRefContext root) {
        final LeafRefContext referencedByContext = getLeafRefReferencedByContext(node, root);
        return referencedByContext == null ? new HashMap<>() : referencedByContext.getAllReferencedByLeafRefCtxs();
    }
}
