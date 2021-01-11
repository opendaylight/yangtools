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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

public class Bug8126Test {
    private static final String FOO_NS = "foo";
    private static final String BAR_NS = "bar";

    @Test
    public void test() throws Exception {
        final EffectiveModelContext context = StmtTestUtils.parseYangSources("/bugs/bug8126");
        assertNotNull(context);
        assertTrue(findNode(context, foo("root"), bar("my-container"), bar("my-choice"), bar("one"), bar("one"),
            bar("mandatory-leaf")) instanceof LeafSchemaNode);
        assertTrue(findNode(context, foo("root"), bar("my-list"), bar("two"), bar("mandatory-leaf-2"))
            instanceof LeafSchemaNode);

        try {
            assertNull(findNode(context, foo("root"), bar("mandatory-list")));
        } catch (final IllegalArgumentException e) {
            assertEquals(String.format("Schema tree child %s not present", bar("mandatory-list")), e.getMessage());
        }
        try {
            assertNull(findNode(context, foo("root"), bar("mandatory-container"), bar("mandatory-choice")));
        } catch (final IllegalArgumentException e) {
            assertEquals(String.format("Schema tree child %s not present", bar("mandatory-container")), e.getMessage());
        }
        try {
            assertNull(findNode(context, foo("root"), bar("mandatory-container-2"), bar("one"),
                    bar("mandatory-leaf-3")));
        } catch (final IllegalArgumentException e) {
            assertEquals(String.format("Schema tree child %s not present", bar("one")), e.getMessage());
        }
    }

    private static SchemaNode findNode(final EffectiveModelContext context, final QName... qnames) {
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterSchemaTree(qnames);
        return SchemaContextUtil.findDataSchemaNode(context, stack);
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, localName);
    }

    private static QName bar(final String localName) {
        return QName.create(BAR_NS, localName);
    }
}
