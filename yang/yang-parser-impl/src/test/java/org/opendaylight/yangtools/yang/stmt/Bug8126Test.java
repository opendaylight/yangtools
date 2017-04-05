/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

public class Bug8126Test {
    private static final String FOO_NS = "foo";
    private static final String BAR_NS = "bar";
    private static final String REV = "1970-01-01";

    @Test
    public void test() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug8126");
        assertNotNull(context);
        assertTrue(findNode(context, ImmutableList.of(foo("root"), bar("my-container"), bar("my-choice"), bar("one"),
                bar("one"), bar("mandatory-leaf"))) instanceof LeafSchemaNode);
        assertTrue(findNode(context, ImmutableList.of(foo("root"), bar("my-list"), bar("two"), bar("mandatory-leaf-2"))) instanceof LeafSchemaNode);
    }

    private static SchemaNode findNode(final SchemaContext context, final Iterable<QName> qNames) {
        return SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(qNames, true));
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, REV, localName);
    }

    private static QName bar(final String localName) {
        return QName.create(BAR_NS, REV, localName);
    }
}
