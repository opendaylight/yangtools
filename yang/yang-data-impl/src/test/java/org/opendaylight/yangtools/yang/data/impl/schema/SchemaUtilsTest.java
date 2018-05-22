/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class SchemaUtilsTest {
    private static final String NS = "my-namespace";

    @Test
    public void test() {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/schema-utils-test/foo.yang");
        assertTrue(SchemaUtils.findDataParentSchemaOnPath(schemaContext,
                SchemaPath.create(true, qN("my-name"), qN("my-name-a"))) instanceof ContainerSchemaNode);
        assertTrue(SchemaUtils.findDataParentSchemaOnPath(schemaContext,
                SchemaPath.create(true, qN("my-name-2"), qN("my-name-b"))) instanceof NotificationDefinition);
        assertTrue(SchemaUtils.findDataParentSchemaOnPath(schemaContext,
                SchemaPath.create(true, qN("my-name-2"), qN("my-name-2-b"))) instanceof ActionDefinition);
    }

    @Test
    public void testNameConflicts() {
        final SchemaContext schemaContext = YangParserTestUtils
                .parseYangResource("/schema-utils-test/name-conflicts.yang");
        // test my-name conflicts
        assertEquals(8, SchemaUtils.findParentSchemaNodesOnPath(schemaContext,
                SchemaPath.create(true, qN("my-name"), qN("my-name-nested"), qN("my-name-nested2"))).size());

        // test target container
        final Collection<SchemaNode> target = SchemaUtils.findParentSchemaNodesOnPath(schemaContext,
                SchemaPath.create(true, qN("my-name-2"), qN("my-name-nested"), qN("target")));
        assertEquals(1, target.size());
        assertTrue(target.iterator().next() instanceof ContainerSchemaNode);

        // test l schema nodes (i.e. container and two leafs)
        Collection<SchemaNode> schema = SchemaUtils.findParentSchemaNodesOnPath(schemaContext,
                SchemaPath.create(true, qN("my-name-3"), qN("input"), qN("con-3"), qN("l")));
        assertEquals(1, schema.size());
        assertTrue(schema.iterator().next() instanceof ContainerSchemaNode);

        schema = SchemaUtils.findParentSchemaNodesOnPath(schemaContext,
                SchemaPath.create(true, qN("my-name-3"), qN("input"), qN("con-1"), qN("l")));
        assertEquals(1, schema.size());
        assertTrue(schema.iterator().next() instanceof LeafSchemaNode);

        schema = SchemaUtils.findParentSchemaNodesOnPath(schemaContext,
                SchemaPath.create(true, qN("my-name-3"), qN("input"), qN("con-2"), qN("l")));
        assertTrue(schema.isEmpty());

        schema = SchemaUtils.findParentSchemaNodesOnPath(schemaContext,
                SchemaPath.create(true, qN("my-name-3"), qN("output"), qN("con-2"), qN("l")));
        assertEquals(1, schema.size());
        assertTrue(schema.iterator().next() instanceof LeafSchemaNode);
    }

    private static QName qN(final String localName) {
        return QName.create(NS, localName);
    }
}
