/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.util;

import java.util.Comparator;
import java.util.Iterator;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

public final class Comparators {

    /**
     * Comparator based on alphabetical order of local name of SchemaNode's
     * qname.
     */
    public static final SchemaNodeComparator SCHEMA_NODE_COMP = new SchemaNodeComparator();

    /**
     * Comparator based on augment target path.
     */
    public static final AugmentComparator AUGMENT_COMP = new AugmentComparator();

    private Comparators() {
    }

    private static final class SchemaNodeComparator implements Comparator<SchemaNode> {
        @Override
        public int compare(final SchemaNode o1, final SchemaNode o2) {
            return o1.getQName().compareTo(o2.getQName());
        }
    }

    private static final class AugmentComparator implements Comparator<AugmentationSchema> {
        @Override
        public int compare(AugmentationSchema augSchema1, AugmentationSchema augSchema2) {
            final Iterator<QName> thisIt = augSchema1.getTargetPath().getPathFromRoot().iterator();
            final Iterator<QName> otherIt = augSchema2.getTargetPath().getPathFromRoot().iterator();

            while (thisIt.hasNext()) {
                if (otherIt.hasNext()) {
                    final int comp = thisIt.next().compareTo(otherIt.next());
                    if (comp != 0) {
                        return comp;
                    }
                } else {
                    return 1;
                }
            }
            if (otherIt.hasNext()) {
                return -1;
            }
            return 0;
        }

    }

}
