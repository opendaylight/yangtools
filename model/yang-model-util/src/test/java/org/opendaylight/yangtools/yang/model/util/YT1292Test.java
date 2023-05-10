/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.google.common.collect.Iterables;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1292Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");
    private static final QName BAZ = QName.create("foo", "baz");
    private static final String YT1292_YANG = """
        module foo {
          namespace foo;
          prefix foo;
          yang-version 1.1;
          rpc foo;
          notification bar;
          container baz {
            action foo;
            notification bar;
          }
        }""";

    private static ModuleEffectiveStatement module;

    private ContainerEffectiveStatement baz;

    @BeforeAll
    static void beforeClass() {
        module = Iterables.getOnlyElement(YangParserTestUtils.parseYang(YT1292_YANG).getModuleStatements()
                .values());
    }

    @BeforeEach
    void before() {
        baz = assertInstanceOf(ContainerEffectiveStatement.class, module.findDataTreeNode(BAZ).orElseThrow());
    }

    @Test
    void testRpc() {
        assertEquals(Optional.empty(), module.findDataTreeNode(FOO));
        assertInstanceOf(RpcEffectiveStatement.class, module.findSchemaTreeNode(FOO).orElseThrow());
    }

    @Test
    void testNotification() {
        assertEquals(Optional.empty(), module.findDataTreeNode(BAR));
        assertInstanceOf(NotificationEffectiveStatement.class, module.findSchemaTreeNode(BAR).orElseThrow());

        assertEquals(Optional.empty(), baz.findDataTreeNode(BAR));
        assertInstanceOf(NotificationEffectiveStatement.class, baz.findSchemaTreeNode(BAR).orElseThrow());
    }

    @Test
    void testAction() {
        assertEquals(Optional.empty(), baz.findDataTreeNode(FOO));
        assertInstanceOf(ActionEffectiveStatement.class, baz.findSchemaTreeNode(FOO).orElseThrow());
    }
}
