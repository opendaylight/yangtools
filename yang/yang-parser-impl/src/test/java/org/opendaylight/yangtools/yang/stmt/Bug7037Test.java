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
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class Bug7037Test {
    private static final String FOO_NS = "foo";
    private static final String BAR_NS = "bar";

    @Test
    public void test() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug7037");
        assertNotNull(context);

        final List<UnknownSchemaNode> unknownSchemaNodes = context.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        final UnknownSchemaNode first = unknownSchemaNodes.iterator().next();
        final List<UnknownSchemaNode> firstUnknownNodes = first.getUnknownSchemaNodes();
        assertEquals(1, firstUnknownNodes.size());

        final UnknownSchemaNode barExtCont = firstUnknownNodes.iterator().next();
        assertEquals(bar("container"), barExtCont.getNodeType());
        assertEquals(foo("bar-ext-con"), barExtCont.getQName());

        final DataSchemaNode root = context.getDataChildByName(foo("root"));
        assertTrue(root instanceof ContainerSchemaNode);

        final List<UnknownSchemaNode> rootUnknownNodes = root.getUnknownSchemaNodes();
        assertEquals(2, rootUnknownNodes.size());

        final Map<QName, UnknownSchemaNode> rootUnknownNodeMap = rootUnknownNodes.stream()
                .collect(Collectors.toMap(u -> u.getNodeType(), u -> u));

        final UnknownSchemaNode barExt = rootUnknownNodeMap.get(bar("bar-ext"));
        final List<UnknownSchemaNode> barExtUnknownNodes = barExt.getUnknownSchemaNodes();
        assertEquals(3, barExtUnknownNodes.size());

        final Iterator<UnknownSchemaNode> iterator = barExtUnknownNodes.iterator();
        UnknownSchemaNode barExtCont2 = null;
        while (iterator.hasNext()) {
            final UnknownSchemaNode next = iterator.next();
            if (bar("container").equals(next.getNodeType())) {
                barExtCont2 = next;
                break;
            }
        }
        assertNotNull(barExtCont2);
        assertEquals(foo("bar-ext-con-2"), barExtCont2.getQName());

        final UnknownSchemaNode fooExt = rootUnknownNodeMap.get(foo("foo-ext"));
        final List<UnknownSchemaNode> fooUnknownNodes = fooExt.getUnknownSchemaNodes();
        assertEquals(1, fooUnknownNodes.size());

        final UnknownSchemaNode fooExtCont = fooUnknownNodes.iterator().next();
        assertEquals(foo("container"), fooExtCont.getNodeType());
        assertEquals(foo("foo-ext-con"), fooExtCont.getQName());
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, localName);
    }

    private static QName bar(final String localName) {
        return QName.create(BAR_NS, localName);
    }
}
