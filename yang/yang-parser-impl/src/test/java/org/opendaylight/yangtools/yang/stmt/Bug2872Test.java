/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

public class Bug2872Test {

    @Test
    public void test() throws Exception {
        final SchemaContext schema = StmtTestUtils.parseYangSources("/bugs/bug2872");
        assertNotNull(schema);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2016-06-08");
        final QNameModule bug2872module = QNameModule.create(new URI("bug2872"), revision);
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
