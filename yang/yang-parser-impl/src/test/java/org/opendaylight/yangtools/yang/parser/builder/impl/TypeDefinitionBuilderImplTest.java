/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.Int16;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;

public class TypeDefinitionBuilderImplTest {

    @Test
    public void getUnknownNodesTest() {

        SchemaPath path = SchemaPath.create(true, QName.create("myModuleName"),QName.create("MyTypeDefName"));
        int line = 1;
        String moduleName = "myModuleName";
        QName qName = QName.create("MyTypeDefName");

        TypeDefinitionBuilderImpl typeDefinitionBuilderImpl = new TypeDefinitionBuilderImpl(moduleName,line,qName,path);
        typeDefinitionBuilderImpl.setType(Int16.getInstance());

        path = SchemaPath.create(true, QName.create("myModuleName"),QName.create("MyUnknownNodeName1"));
        line = 1;
        moduleName = "myModuleName";
        qName = QName.create("MyUnknownNodeName1");

        UnknownSchemaNodeBuilderImpl unknownSchemaNodeBuilder1 = new UnknownSchemaNodeBuilderImpl(moduleName,line,qName,path);

        path = SchemaPath.create(true, QName.create("myModuleName"),QName.create("MyUnknownNodeName2"));
        line = 2;
        moduleName = "myModuleName";
        qName = QName.create("MyUnknownNodeName2");

        UnknownSchemaNodeBuilderImpl unknownSchemaNodeBuilder2 = new UnknownSchemaNodeBuilderImpl(moduleName,line,qName,path);

        typeDefinitionBuilderImpl.addUnknownNodeBuilder(unknownSchemaNodeBuilder1);
        typeDefinitionBuilderImpl.addUnknownNodeBuilder(unknownSchemaNodeBuilder2);

        List<UnknownSchemaNodeBuilder> unknownNodesBuilders = typeDefinitionBuilderImpl.getUnknownNodes();

        assertNotNull(unknownNodesBuilders);
        assertFalse(unknownNodesBuilders.isEmpty());
        assertEquals(2,unknownNodesBuilders.size());
        assertTrue(unknownNodesBuilders.contains(unknownSchemaNodeBuilder1));
        assertTrue(unknownNodesBuilders.contains(unknownSchemaNodeBuilder2));

        TypeDefinition<? extends TypeDefinition<?>> instance = typeDefinitionBuilderImpl.build();

        List<UnknownSchemaNode> unknownNodes = instance.getUnknownSchemaNodes();

        assertNotNull(unknownNodes);
        assertFalse(unknownNodes.isEmpty());
        assertEquals(2,unknownNodes.size());
        assertTrue(unknownNodes.contains(unknownSchemaNodeBuilder1.build()));
        assertTrue(unknownNodes.contains(unknownSchemaNodeBuilder2.build()));

    }

}
