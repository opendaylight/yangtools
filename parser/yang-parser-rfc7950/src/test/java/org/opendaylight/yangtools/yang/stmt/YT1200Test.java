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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

class YT1200Test extends AbstractYangTest {
    private static final QName FOO = QName.create("urn:foo", "foo");

    @Test
    void testKeyParsing() {
        final DataSchemaNode foo = assertEffectiveModel("/bugs/YT1200/foo.yang").getDataChildByName(FOO);
        assertEquals(List.of(FOO, QName.create(FOO, "bar"), QName.create(FOO, "baz")),
            assertInstanceOf(ListSchemaNode.class, foo).getKeyDefinition());
    }
}
