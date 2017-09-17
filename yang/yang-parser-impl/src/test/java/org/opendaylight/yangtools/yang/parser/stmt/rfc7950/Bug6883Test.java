/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6883Test {
    private static final String FOO_NS = "foo";
    private static final String FOO_REV = "1970-01-01";

    @Test
    public void test() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources("/rfc7950/bug6883");
        assertNotNull(schemaContext);

        final AnyDataSchemaNode topAnyData = assertAnyData(schemaContext, ImmutableList.of("top"));
        assertEquals(Status.DEPRECATED, topAnyData.getStatus());
        assertEquals("top anydata", topAnyData.getDescription());

        assertAnyData(schemaContext, ImmutableList.of("root", "root-anydata"));
        assertAnyData(schemaContext, ImmutableList.of("root", "aug-anydata"));
        assertAnyData(schemaContext, ImmutableList.of("root", "grp-anydata"));
        assertAnyData(schemaContext, ImmutableList.of("my-list", "list-anydata"));
        assertAnyData(schemaContext, ImmutableList.of("sub-data"));

        assertAnyData(schemaContext, ImmutableList.of("my-rpc", "input", "input-anydata"));
        assertAnyData(schemaContext, ImmutableList.of("my-rpc", "output", "output-anydata"));
        assertAnyData(schemaContext, ImmutableList.of("my-notification", "notification-anydata"));

        assertAnyData(schemaContext, ImmutableList.of("my-choice", "one", "case-anydata"));
        assertAnyData(schemaContext, ImmutableList.of("my-choice", "case-shorthand-anydata", "case-shorthand-anydata"));
    }

    private static AnyDataSchemaNode assertAnyData(final SchemaContext context, final Iterable<String> localNamesPath) {
        final Iterable<QName> qNames = Iterables.transform(localNamesPath,
            localName -> QName.create(FOO_NS, FOO_REV, localName));
        final SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(qNames, true));
        assertTrue(findDataSchemaNode instanceof AnyDataSchemaNode);
        return (AnyDataSchemaNode) findDataSchemaNode;
    }
}