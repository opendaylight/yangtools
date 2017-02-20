/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

public class Bug5101Test {
    private static final String NS = "foo";
    private static final String REV = "2016-01-29";

    @Test
    public void test() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5101");
        assertNotNull(context);

        QName grp = QName.create(NS, REV, "my-grouping");
        QName myContainer = QName.create(NS, REV, "my-container");
        QName root = QName.create(NS, REV, "root");

        SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, grp, myContainer));
        assertTrue(findDataSchemaNode instanceof ContainerSchemaNode);
        ContainerSchemaNode myContainerInGrouping = (ContainerSchemaNode) findDataSchemaNode;
        assertEquals(Status.DEPRECATED, myContainerInGrouping.getStatus());

        findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true, root, myContainer));
        assertTrue(findDataSchemaNode instanceof ContainerSchemaNode);
        ContainerSchemaNode myContainerInRoot = (ContainerSchemaNode) findDataSchemaNode;
        assertEquals(Status.DEPRECATED, myContainerInRoot.getStatus());

        findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true, myContainer));
        assertTrue(findDataSchemaNode instanceof ContainerSchemaNode);
        ContainerSchemaNode myContainerInModule = (ContainerSchemaNode) findDataSchemaNode;
        assertEquals(Status.DEPRECATED, myContainerInModule.getStatus());

        findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true, root));
        assertTrue(findDataSchemaNode instanceof ContainerSchemaNode);
        ContainerSchemaNode rootContainer = (ContainerSchemaNode) findDataSchemaNode;
        assertEquals(Status.CURRENT, rootContainer.getStatus());
    }
}
