/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6884Test {
    private static final String FOO_NS = "foo";
    private static final String FOO_REV = "1970-01-01";

    @Test
    public void testYang11() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources("/rfc7950/bug6884/yang1-1");
        assertNotNull(schemaContext);

        assertTrue(findNode(schemaContext, ImmutableList.of(foo("sub-root"), foo("sub-foo-2-con")))
            instanceof ContainerSchemaNode);
    }

    @Test
    public void testCircularIncludesYang10() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources("/rfc7950/bug6884/circular-includes");

        assertNotNull(schemaContext);
        assertTrue(findNode(schemaContext, ImmutableList.of(foo("sub-root"), foo("sub-foo-2-con")))
            instanceof ContainerSchemaNode);
        assertTrue(findNode(schemaContext, ImmutableList.of(foo("sub-root-2"), foo("sub-foo-con")))
            instanceof ContainerSchemaNode);
    }

    private static SchemaNode findNode(final SchemaContext context, final Iterable<QName> qnames) {
        return SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(qnames, true));
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, FOO_REV, localName);
    }
}
