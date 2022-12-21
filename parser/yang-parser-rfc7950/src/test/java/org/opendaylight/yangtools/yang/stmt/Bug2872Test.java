/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

class Bug2872Test extends AbstractYangTest {
    @Test
    void test() {
        final var schema = assertEffectiveModelDir("/bugs/bug2872");

        final QNameModule bug2872module = QNameModule.create(XMLNamespace.of("bug2872"), Revision.of("2016-06-08"));
        final QName foo = QName.create(bug2872module, "bar");

        final DataSchemaNode dataSchemaNode = schema.getDataChildByName(foo);
        assertTrue(dataSchemaNode instanceof LeafSchemaNode);
        final LeafSchemaNode myLeaf = (LeafSchemaNode) dataSchemaNode;

        final TypeDefinition<?> type = myLeaf.getType();
        assertTrue(type instanceof EnumTypeDefinition);
        final EnumTypeDefinition myEnum = (EnumTypeDefinition) type;

        final List<EnumTypeDefinition.EnumPair> values = myEnum.getValues();
        assertEquals(2, values.size());

        final List<String> valueNames = new ArrayList<>();
        for (EnumTypeDefinition.EnumPair pair : values) {
            valueNames.add(pair.getName());
        }
        assertTrue(valueNames.contains("value-one"));
        assertTrue(valueNames.contains("value-two"));
    }
}
