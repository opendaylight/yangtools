/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;

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

    private static LeafRefContext getLeafRefReferencingContext(
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

//
//    LeafRefContext getLeafRefReferencedByContext(SchemaNode node, LeafRefContext root){
//
//    }
//
//
//
//
//    isLeafRef
//
//    isReferencedByLeafRef
//
//    getReferencedByLeafRef
//
//
//    hasLeafRefChild
//
//    hasChildReferencedByLeafRef


}
