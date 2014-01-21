/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.net.URI;
import java.util.Comparator;
import java.util.Date;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;

public final class Comparators {

    /**
     * Comparator based on alphabetical order of qname's local name.
     */
    public static final QNameComparator QNAME_COMP = new QNameComparator();

    /**
     * Comparator based on alphabetical order of local name of SchemaNode's qname.
     */
    public static final SchemaNodeComparator SCHEMA_NODE_COMP = new SchemaNodeComparator();

    /**
     * Comparator based on augment target path length.
     */
    public static final AugmentComparator AUGMENT_COMP = new AugmentComparator();

    private Comparators() {
    }

    private static final class QNameComparator implements Comparator<QName> {
        @Override
        public int compare(QName o1, QName o2) {
            return o1.getLocalName().compareTo(o2.getLocalName());
        }
    }

    private static final class SchemaNodeComparator implements Comparator<SchemaNode> {
        @Override
        public int compare(SchemaNode o1, SchemaNode o2) {
            QName q1 = o1.getQName();
            QName q2 = o2.getQName();
            int result = q1.getLocalName().compareTo(q2.getLocalName());
            if (result == 0) {
                URI ns1 = q1.getNamespace();
                URI ns2 = q2.getNamespace();
                if (ns1 == null && ns2 == null) {
                    Date rev1 = q1.getRevision();
                    Date rev2 = q2.getRevision();

                    if (rev1 == null && rev2 == null) {
                        String p1 = q1.getPrefix();
                        String p2 = q2.getPrefix();
                        if (p1 == null && p2 == null) {
                            throw new IllegalArgumentException("Failed to sort nodes: " + o1 + ", " + o2);
                        }
                        if (p1 == null || p2 == null) {
                            if (p1 == null) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                        return p1.compareTo(p2);
                    }
                    if (rev1 == null || rev2 == null) {
                        if (rev1 == null) {
                            return -1;
                        } else {
                            return -2;
                        }
                    }
                    return rev1.compareTo(rev2);
                }
                if (ns1 == null || ns2 == null) {
                    if (ns1 == null) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
                return ns1.toString().compareTo(ns2.toString());
            } else {
                return result;
            }
        }
    }

    private static final class AugmentComparator implements Comparator<AugmentationSchemaBuilder> {
        @Override
        public int compare(AugmentationSchemaBuilder o1, AugmentationSchemaBuilder o2) {
            return o1.getTargetPath().getPath().size() - o2.getTargetPath().getPath().size();
        }

    }

}
