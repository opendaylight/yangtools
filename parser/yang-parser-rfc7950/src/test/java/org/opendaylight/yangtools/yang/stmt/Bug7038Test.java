/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

public class Bug7038Test extends AbstractYangTest {
    @Test
    public void unknownNodeTest() {
        final ModuleStatement bar = assertEffectiveModelDir("/bugs/bug7038")
            .getModuleStatement(QNameModule.create(XMLNamespace.of("bar"))).getDeclared();
        final UnrecognizedStatement decimal64 = bar.findFirstDeclaredSubstatement(UnrecognizedStatement.class)
            .orElseThrow();
        assertEquals("decimal64", decimal64.argument());
        assertEquals(QName.create("foo", "decimal64"), decimal64.statementDefinition().getStatementName());
    }

    @Test
    public void testYang11() throws Exception {
        final ContainerSchemaNode root = (ContainerSchemaNode) assertEffectiveModelDir("/bugs/bug7038/yang11")
            .getDataChildByName(QName.create("foo", "root"));
        final TypeDefinition<?> typedef = ((LeafSchemaNode) root.getDataChildByName(QName.create("foo", "my-leafref")))
            .getType();
        assertThat(typedef, instanceOf(LeafrefTypeDefinition.class));
        assertFalse(((LeafrefTypeDefinition) typedef).requireInstance());
    }

    @Test
    public void testYang10() throws Exception {
        assertInvalidSubstatementExceptionDir("/bugs/bug7038/yang10",
            startsWith("REQUIRE_INSTANCE is not valid for TYPE"));
    }
}
