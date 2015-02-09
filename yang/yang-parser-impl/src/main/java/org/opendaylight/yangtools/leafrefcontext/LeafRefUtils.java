/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;

import java.util.LinkedList;

import java.util.Iterator;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class LeafRefUtils {

    /**
     * @param leafRefTargetPath
     * @param currentNodePath
     * @return
     */
    public static LeafRefPath transformToAbsoluteLeafRefPath(
            LeafRefPath leafRefTargetPath, SchemaPath currentNodePath) {

        LinkedList<QNameWithPredicate> absoluteLeafRefTargetPath = new LinkedList<QNameWithPredicate>();

        Iterable<QName> nodePathFromRoot = currentNodePath.getPathFromRoot();
        while(nodePathFromRoot.iterator().hasNext()){

        }

        Iterable<QNameWithPredicate> leafRefTargetPathFromRoot = leafRefTargetPath.getPathFromRoot();

        Iterator<QNameWithPredicate> leafRefTgtPathFromRootIterator = leafRefTargetPathFromRoot.iterator();

        while(leafRefTgtPathFromRootIterator.hasNext()) {

            QNameWithPredicate nextLeafRefTgtPathQName = leafRefTgtPathFromRootIterator.next();
            if(!nextLeafRefTgtPathQName.equals(QNameWithPredicate.UP_PARENT)) break;

        }

        return null;
    }

}
