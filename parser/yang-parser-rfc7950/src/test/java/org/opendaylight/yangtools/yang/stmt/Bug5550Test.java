/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

public class Bug5550Test {
    private static final String NS = "foo";
    private static final String REV = "2016-03-18";

    @Test
    public void test() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5550");
        assertNotNull(context);

        QName root = QName.create(NS, REV, "root");
        QName containerInGrouping = QName.create(NS, REV, "container-in-grouping");
        QName leaf1 = QName.create(NS, REV, "leaf-1");

        SchemaNode findDataSchemaNode = context.findDataTreeChild(root, containerInGrouping, leaf1).get();
        assertThat(findDataSchemaNode, instanceOf(LeafSchemaNode.class));
    }
}
