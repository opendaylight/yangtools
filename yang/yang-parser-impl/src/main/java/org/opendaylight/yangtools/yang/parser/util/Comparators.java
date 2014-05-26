/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.util.Comparator;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;

public final class Comparators {

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

    private static final class SchemaNodeComparator implements Comparator<SchemaNode> {
        @Override
        public int compare(SchemaNode o1, SchemaNode o2) {
            return o1.getQName().compareTo(o2.getQName());
        }
    }

    private static final class AugmentComparator implements Comparator<AugmentationSchemaBuilder> {
        @Override
        public int compare(AugmentationSchemaBuilder o1, AugmentationSchemaBuilder o2) {
            return o1.getTargetPath().getPath().size() - o2.getTargetPath().getPath().size();
        }

    }

}
