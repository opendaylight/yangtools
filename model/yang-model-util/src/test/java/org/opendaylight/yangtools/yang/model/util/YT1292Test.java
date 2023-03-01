/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Iterables;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1292Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");
    private static final QName BAZ = QName.create("foo", "baz");

    private static ModuleEffectiveStatement module;

    private ContainerEffectiveStatement baz;

    @BeforeAll
    static void beforeClass() {
        module = Iterables.getOnlyElement(YangParserTestUtils.parseYangResource("/yt1292.yang").getModuleStatements()
                .values());
    }

    @BeforeEach
    void before() {
        final DataTreeEffectiveStatement<?> tmp = module.findDataTreeNode(BAZ).orElseThrow();
        assertThat(tmp, instanceOf(ContainerEffectiveStatement.class));
        baz = (ContainerEffectiveStatement) tmp;
    }

    @Test
    void testRpc() {
        assertEquals(Optional.empty(), module.findDataTreeNode(FOO));
        final SchemaTreeEffectiveStatement<?> foo = module.findSchemaTreeNode(FOO).orElseThrow();
        assertThat(foo, instanceOf(RpcEffectiveStatement.class));
    }

    @Test
    void testNotification() {
        assertEquals(Optional.empty(), module.findDataTreeNode(BAR));
        SchemaTreeEffectiveStatement<?> bar = module.findSchemaTreeNode(BAR).orElseThrow();
        assertThat(bar, instanceOf(NotificationEffectiveStatement.class));

        assertEquals(Optional.empty(), baz.findDataTreeNode(BAR));
        bar = baz.findSchemaTreeNode(BAR).orElseThrow();
        assertThat(bar, instanceOf(NotificationEffectiveStatement.class));
    }

    @Test
    void testAction() {
        assertEquals(Optional.empty(), baz.findDataTreeNode(FOO));
        final SchemaTreeEffectiveStatement<?> foo = baz.findSchemaTreeNode(FOO).orElseThrow();
        assertThat(foo, instanceOf(ActionEffectiveStatement.class));
    }
}
