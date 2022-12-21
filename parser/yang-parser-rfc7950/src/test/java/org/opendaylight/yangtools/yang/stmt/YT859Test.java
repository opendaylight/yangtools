/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.common.collect.Iterables;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;

class YT859Test {
    @Test
    void testAugmentUnsupported() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/YT859/", Set.of(),
            YangParserConfiguration.DEFAULT);
        assertEquals(4, context.getModules().size());

        final DataSchemaNode named = Iterables.getOnlyElement(context.findModules("xyzzy"))
            .findDataChildByName(QName.create("xyzzy", "xyzzy"), QName.create("xyzzy", "named")).orElseThrow();
        assertNull(assertInstanceOf(ListSchemaNode.class, named).dataChildByName(QName.create("foo", "foo")));
    }
}
