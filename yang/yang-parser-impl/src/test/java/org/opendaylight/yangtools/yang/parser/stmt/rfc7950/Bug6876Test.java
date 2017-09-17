/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6876Test {
    private static final String BAR_NS = "bar";
    private static final String BAR_REV = "2017-01-11";
    private static final String FOO_NS = "foo";
    private static final String FOO_REV = "1970-01-01";

    @Test
    public void yang11Test() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/rfc7950/bug6876/yang11");
        assertNotNull(context);

        assertTrue(findNode(context, ImmutableList.of(bar("augment-target"),
            bar("my-leaf"))) instanceof LeafSchemaNode);
        assertTrue(findNode(context, ImmutableList.of(bar("augment-target"),
            foo("mandatory-leaf"))) instanceof LeafSchemaNode);
    }

    @Test
    public void yang10Test() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/rfc7950/bug6876/yang10");
        assertNotNull(context);

        assertTrue(findNode(context, ImmutableList.of(bar("augment-target"),
            bar("my-leaf"))) instanceof LeafSchemaNode);
        assertNull(findNode(context, ImmutableList.of(bar("augment-target"), foo("mandatory-leaf"))));
    }

    private static SchemaNode findNode(final SchemaContext context, final Iterable<QName> qnames) {
        return SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(qnames, true));
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, FOO_REV, localName);
    }

    private static QName bar(final String localName) {
        return QName.create(BAR_NS, BAR_REV, localName);
    }
}
