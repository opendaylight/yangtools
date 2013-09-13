/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.util.Comparator;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;

public final class Comparators {

    public static final QNameComparator QNAME_COMP = new QNameComparator();
    public static final SchemaNodeComparator SCHEMA_NODE_COMP = new SchemaNodeComparator();
    public static final UsesNodeComparator USES_NODE_COMP = new UsesNodeComparator();

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
            return o1.getQName().getLocalName().compareTo(o2.getQName().getLocalName());
        }
    }

    private static final class UsesNodeComparator implements Comparator<UsesNodeBuilder> {
        @Override
        public int compare(UsesNodeBuilder o1, UsesNodeBuilder o2) {
            int x = countChildUses(o1);
            int y = countChildUses(o2);
            return x - y;
        }
    }

    private static int countChildUses(UsesNodeBuilder usesNode) {
        GroupingBuilder grouping = usesNode.getGroupingBuilder();
        int x = grouping.getUsesNodes().size();
        for(UsesNodeBuilder u : grouping.getUsesNodes()) {
            x += countChildUses(u);
        }
        return x;
    }

}
