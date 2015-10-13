/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug4459Test {

    @Test
    public void test() throws IOException, URISyntaxException, SourceException, ReactorException {
        SchemaContext schema = StmtTestUtils.parseYangSources("/bugs/bug4459");
        assertNotNull(schema);

        DataSchemaNode dataChildByName = schema.getDataChildByName("my-leaf");
        assertTrue(dataChildByName instanceof LeafSchemaNode);
        LeafSchemaNode myLeaf = (LeafSchemaNode) dataChildByName;

        TypeDefinition<?> type = myLeaf.getType();
        assertTrue(type instanceof EnumTypeDefinition);
        EnumTypeDefinition myEnum = (EnumTypeDefinition) type;

        QName expectedEnumQName = QName.create("foo", "1970-01-01", "QuestionMark");
        List<EnumPair> values = myEnum.getValues();
        for (EnumPair enumPair : values) {
            if (enumPair.getName().equals("?")) {
                assertEquals(expectedEnumQName, enumPair.getQName());
            }
        }
    }
}
