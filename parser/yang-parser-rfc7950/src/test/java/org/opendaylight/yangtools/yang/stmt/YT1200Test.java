/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class YT1200Test {
    private static final QName FOO = QName.create("urn:foo", "foo");

    @Test
    public void testKeyParsing() throws Exception {
        final DataSchemaNode foo = StmtTestUtils.parseYangSource("/bugs/YT1200/foo.yang").getDataChildByName(FOO);
        assertThat(foo, instanceOf(ListSchemaNode.class));
        assertEquals(List.of(FOO, QName.create(FOO, "bar"), QName.create(FOO, "baz")),
            ((ListSchemaNode) foo).getKeyDefinition());
    }
}
