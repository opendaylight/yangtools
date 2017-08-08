/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
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

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

public class Bug8922Test {
    private static final String NS = "foo";
    private static final String REV = "1970-01-01";

    @Test
    public void testAllFeaturesSupported() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSource("/bugs/bug8922/foo.yang");
        assertNotNull(context);
        final SchemaNode findNode = findNode(context, qN("target"), qN("my-con"));
        assertTrue(findNode instanceof ContainerSchemaNode);
        assertEquals("New description", ((ContainerSchemaNode) findNode).getDescription());
    }

    @Test
    public void testNoFeatureSupported() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSource("/bugs/bug8922/foo.yang", ImmutableSet.of());
        assertNotNull(context);
        final SchemaNode findNode = findNode(context, qN("target"), qN("my-con"));
        assertNull(findNode);
        assertTrue(context.getAvailableAugmentations().isEmpty());
    }

    private static SchemaNode findNode(final SchemaContext context, final QName... qNames) {
        return SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true, qNames));
    }

    private static QName qN(final String localName) {
        return QName.create(NS, REV, localName);
    }
}
