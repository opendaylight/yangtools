/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6883Test extends AbstractYangTest {
    private static final XMLNamespace FOO = XMLNamespace.of("foo");

    private ModuleEffectiveStatement foo;

    @BeforeEach
    void before() {
        foo = assertEffectiveModelDir("/bugs/bug6883").getModuleStatement(QName.create(FOO, "foo"));
    }

    @Test
    void test() throws Exception {
        final AnydataSchemaNode topAnyData = assertAnyData("top");
        assertEquals(Status.DEPRECATED, topAnyData.getStatus());
        assertEquals(Optional.of("top anydata"), topAnyData.getDescription());

        assertAnyData("root", "root-anydata");
        assertAnyData("root", "aug-anydata");
        assertAnyData("root", "grp-anydata");
        assertAnyData("my-list", "list-anydata");
        assertAnyData("sub-data");

        assertAnyData("my-rpc", "input", "input-anydata");
        assertAnyData("my-rpc", "output", "output-anydata");
        assertAnyData("my-notification", "notification-anydata");

        assertAnyData("my-choice", "one", "case-anydata");
        assertAnyData("my-choice", "case-shorthand-anydata", "case-shorthand-anydata");
    }

    private AnydataSchemaNode assertAnyData(final String... names) {
        final SchemaTreeEffectiveStatement<?> stmt = foo.findSchemaTreeNode(Arrays.stream(names)
            .map(name -> QName.create(FOO, name))
            .collect(Collectors.toList()))
            .orElse(null);
        assertThat(stmt, instanceOf(AnydataSchemaNode.class));
        return (AnydataSchemaNode) stmt;
    }
}