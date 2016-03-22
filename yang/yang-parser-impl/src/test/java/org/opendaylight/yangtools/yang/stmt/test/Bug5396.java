/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug5396 {
    @Test
    public void test() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5396");
        assertNotNull(context);

        QName root = QName.create("foo", "1970-01-01", "root");
        QName myLeaf2 = QName.create("foo", "1970-01-01", "my-leaf2");

        SchemaPath schemaPath = SchemaPath.create(true, root, myLeaf2);
        SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context, schemaPath);
        assertTrue(findDataSchemaNode instanceof LeafSchemaNode);

        LeafSchemaNode leaf2 = (LeafSchemaNode) findDataSchemaNode;
        TypeDefinition<?> type = leaf2.getType();
        assertTrue(type instanceof UnionTypeDefinition);

        UnionTypeDefinition union = (UnionTypeDefinition) type;
        List<TypeDefinition<?>> types = union.getTypes();

        assertEquals(4, types.size());

        TypeDefinition<?> type0 = types.get(0);
        TypeDefinition<?> type1 = types.get(1);
        TypeDefinition<?> type2 = types.get(2);
        TypeDefinition<?> type3 = types.get(3);

        assertFalse(type0.equals(type1));
        assertFalse(type0.equals(type2));
        assertFalse(type0.equals(type3));
    }
}
