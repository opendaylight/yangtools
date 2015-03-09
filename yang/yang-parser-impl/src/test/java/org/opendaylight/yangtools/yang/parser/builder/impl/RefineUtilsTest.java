/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

/**
 * Test suite for increasing of test coverage of RefineUtils implementation.
 *
 * @see org.opendaylight.yangtools.yang.parser.builder.impl.RefineUtils
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
public class RefineUtilsTest extends AbstractBuilderTest {

    @Test
    public void testRefineLeafWithEmptyDefaultString() {
        final String leafLocalName = "leaf-to-refine";
        final QName leafName = QName.create(module.getNamespace(), module.getRevision(), leafLocalName);
        final SchemaPath leafPath = SchemaPath.create(true, leafName);
        final LeafSchemaNodeBuilder leafBuilder = new LeafSchemaNodeBuilder(module.getModuleName(), 22, leafName, leafPath);

        final RefineHolderImpl refineBuilder = new RefineHolderImpl(module.getModuleName(), 23, "/"+leafLocalName);
        refineBuilder.setDefaultStr("");

        RefineUtils.performRefine(leafBuilder, refineBuilder);
        assertNull(leafBuilder.getDefaultStr());
    }

    @Test
    public void testRefineContainer() {
        final String containerLocalName = "container-to-refine";
        final QName containerName = QName.create(module.getNamespace(), module.getRevision(), containerLocalName);
        final SchemaPath containerPath = SchemaPath.create(true, containerName);
        final ContainerSchemaNodeBuilder containerBuilder = new ContainerSchemaNodeBuilder(module.getModuleName(),
            10, containerName, containerPath);

        final RefineHolderImpl refineBuilder = new RefineHolderImpl(module.getModuleName(), 23, "/"+containerLocalName);
        refineBuilder.setPresence(null);

        final MustDefinition must = provideMustDefinition();
        refineBuilder.setMust(must);

        final UnknownSchemaNodeBuilder unknownNodeBuilder = provideUnknownNodeBuilder();
        refineBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        RefineUtils.performRefine(containerBuilder, refineBuilder);

        assertFalse(containerBuilder.getConstraints().getMustDefinitions().isEmpty());
        assertEquals(containerBuilder.getConstraints().getMustDefinitions().size(), 1);
        assertFalse(containerBuilder.getUnknownNodes().isEmpty());
        assertEquals(containerBuilder.getUnknownNodes().get(0), unknownNodeBuilder);
    }



    @Test
    public void testRefineList() {
        final String listLocalName = "list-to-refine";
        final QName listQName = QName.create(module.getNamespace(), module.getRevision(), listLocalName);
        final SchemaPath listPath = SchemaPath.create(true, listQName);
        final ListSchemaNodeBuilder listBuilder = new ListSchemaNodeBuilder(module.getModuleName(),
            10, listQName, listPath);

        final RefineHolderImpl refineBuilder = new RefineHolderImpl(module.getModuleName(), 23, "/"+listLocalName);

        final MustDefinition must = provideMustDefinition();
        refineBuilder.setMust(must);

        final UnknownSchemaNodeBuilder unknownNodeBuilder = provideUnknownNodeBuilder();
        refineBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        refineBuilder.setMinElements(null);
        refineBuilder.setMaxElements(null);

        RefineUtils.performRefine(listBuilder, refineBuilder);
        assertFalse(listBuilder.getConstraints().getMustDefinitions().isEmpty());
        assertEquals(listBuilder.getConstraints().getMustDefinitions().size(), 1);
        assertEquals(0, listBuilder.getConstraints().getMinElements().intValue());
        assertEquals(Integer.MAX_VALUE, listBuilder.getConstraints().getMaxElements().intValue());
        assertFalse(listBuilder.getUnknownNodes().isEmpty());
        assertEquals(listBuilder.getUnknownNodes().get(0), unknownNodeBuilder);
    }

    @Test
    public void testRefineLeafList() {
        final String leafListLocalName = "list-to-refine";
        final QName leafListQName = QName.create(module.getNamespace(), module.getRevision(), leafListLocalName);
        final SchemaPath leafListPath = SchemaPath.create(true, leafListQName);
        final LeafListSchemaNodeBuilder leafListBuilder = new LeafListSchemaNodeBuilder(module.getModuleName(),
            10, leafListQName, leafListPath);

        final RefineHolderImpl refineBuilder = new RefineHolderImpl(module.getModuleName(), 23, "/"+leafListLocalName);

        final MustDefinition must = provideMustDefinition();
        refineBuilder.setMust(null);

        final UnknownSchemaNodeBuilder unknownNodeBuilder = provideUnknownNodeBuilder();
        refineBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        refineBuilder.setMinElements(null);
        refineBuilder.setMaxElements(null);

        RefineUtils.performRefine(leafListBuilder, refineBuilder);
        assertTrue(leafListBuilder.getConstraints().getMustDefinitions().isEmpty());
        assertEquals(0, leafListBuilder.getConstraints().getMinElements().intValue());
        assertEquals(Integer.MAX_VALUE, leafListBuilder.getConstraints().getMaxElements().intValue());
        assertFalse(leafListBuilder.getUnknownNodes().isEmpty());
        assertEquals(leafListBuilder.getUnknownNodes().get(0), unknownNodeBuilder);

        refineBuilder.setMinElements(Integer.MIN_VALUE);
        refineBuilder.setMaxElements(Integer.MAX_VALUE);
        refineBuilder.setMust(must);

        RefineUtils.performRefine(leafListBuilder, refineBuilder);
        assertFalse(leafListBuilder.getConstraints().getMustDefinitions().isEmpty());
        assertEquals(leafListBuilder.getConstraints().getMustDefinitions().size(), 1);

        assertNotNull(leafListBuilder.getConstraints().getMinElements());
        assertNotNull(leafListBuilder.getConstraints().getMaxElements());
    }

    @Test
    public void testRefineChoice() {
        final String choiceLocalName = "choice-to-refine";
        final ChoiceBuilder choiceBuilder = provideChoiceBuilder(choiceLocalName);

        final RefineHolderImpl refineBuilder = new RefineHolderImpl(module.getModuleName(), 23, "/"+choiceLocalName);
        refineBuilder.setDefaultStr(null);
        refineBuilder.setMandatory(null);

        RefineUtils.performRefine(choiceBuilder, refineBuilder);
        assertNull(choiceBuilder.getDefaultCase());
        assertFalse(choiceBuilder.getConstraints().isMandatory());

        final String defaultValue = "choice-default-case";
        refineBuilder.setDefaultStr(defaultValue);
        refineBuilder.setMandatory(true);
        final UnknownSchemaNodeBuilder unknownNodeBuilder = provideUnknownNodeBuilder();
        refineBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        RefineUtils.performRefine(choiceBuilder, refineBuilder);
        assertNotNull(choiceBuilder.getDefaultCase());
        assertNotNull(choiceBuilder.getConstraints().isMandatory());
        assertFalse(choiceBuilder.getUnknownNodes().isEmpty());
        assertEquals(choiceBuilder.getUnknownNodes().get(0), unknownNodeBuilder);
    }

    @Test
    public void testRefineAnyxml() {
        final String anyxmlLocalName = "anyxml-to-refine";
        final QName anyxmlName = QName.create(module.getNamespace(), module.getRevision(), anyxmlLocalName);
        final SchemaPath anyxmlPath = SchemaPath.create(true, anyxmlName);
        final AnyXmlBuilder anyXmlBuilder = new AnyXmlBuilder(module.getModuleName(), 22, anyxmlName, anyxmlPath);

        final RefineHolderImpl refineBuilder = new RefineHolderImpl(module.getModuleName(), 23, "/"+anyxmlLocalName);
        refineBuilder.setMandatory(null);
        refineBuilder.setMust(null);

        RefineUtils.performRefine(anyXmlBuilder, refineBuilder);
        assertNull(refineBuilder.isMandatory());
        assertNull(refineBuilder.getMust());

        final MustDefinition must = provideMustDefinition();

        refineBuilder.setMandatory(true);
        refineBuilder.setMust(must);

        final UnknownSchemaNodeBuilder unknownNodeBuilder = provideUnknownNodeBuilder();
        refineBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        RefineUtils.performRefine(anyXmlBuilder, refineBuilder);
        assertFalse(anyXmlBuilder.getConstraints().getMustDefinitions().isEmpty());
        assertEquals(anyXmlBuilder.getConstraints().getMustDefinitions().size(), 1);
        assertFalse(anyXmlBuilder.getUnknownNodes().isEmpty());
        assertEquals(anyXmlBuilder.getUnknownNodes().get(0), unknownNodeBuilder);
        assertTrue(anyXmlBuilder.getConstraints().isMandatory());
    }

    @Test(expected = YangParseException.class)
    public void testCheckRefineDefault() {
        final String groupLocalName = "test-group";
        final QName testGroup = QName.create(module.getNamespace(), module.getRevision(), groupLocalName);
        final SchemaPath groupPath = SchemaPath.create(true, testGroup);
        final GroupingBuilder grouping = module.addGrouping(12, testGroup, groupPath);

        final RefineHolderImpl refineBuilder = new RefineHolderImpl(module.getModuleName(), 23, "/"+groupLocalName);
        refineBuilder.setDefaultStr("invalid-default-value");

        RefineUtils.performRefine(grouping, refineBuilder);
    }

    @Test(expected = YangParseException.class)
    public void testCheckRefineMandatory() {
        final TypeDefinitionBuilderImpl typedef = initTypedef();
        final RefineHolderImpl refineBuilder = new RefineHolderImpl(module.getModuleName(), 23, "/"+typedef.getQName().getLocalName());
        refineBuilder.setMandatory(true);
        RefineUtils.performRefine(typedef, refineBuilder);
    }

    private TypeDefinitionBuilderImpl initTypedef() {
        final String typedefLocalName = "test-type-definition";
        final QName testTypedef = QName.create(module.getNamespace(), module.getRevision(), typedefLocalName);
        SchemaPath testTypedefPath = SchemaPath.create(true, testTypedef);

        return module.addTypedef(23, testTypedef, testTypedefPath);
    }

    @Test(expected = YangParseException.class)
    public void testCheckRefineMust() {
        final TypeDefinitionBuilderImpl typedef = initTypedef();
        final RefineHolderImpl refineBuilder = new RefineHolderImpl(module.getModuleName(), 23, "/"+typedef.getQName().getLocalName());

        final MustDefinition must = provideMustDefinition();
        refineBuilder.setMust(must);
        RefineUtils.performRefine(typedef, refineBuilder);
    }

    @Test(expected = YangParseException.class)
    public void testCheckRefineMin() {
        final TypeDefinitionBuilderImpl typedef = initTypedef();
        final RefineHolderImpl refineBuilder = new RefineHolderImpl(module.getModuleName(), 23, "/"+typedef.getQName().getLocalName());

        refineBuilder.setMinElements(Integer.MIN_VALUE);
        RefineUtils.performRefine(typedef, refineBuilder);
    }

    @Test(expected = YangParseException.class)
    public void testCheckRefineMax() {
        final TypeDefinitionBuilderImpl typedef = initTypedef();
        final RefineHolderImpl refineBuilder = new RefineHolderImpl(module.getModuleName(), 23, "/"+typedef.getQName().getLocalName());

        refineBuilder.setMaxElements(Integer.MAX_VALUE);
        RefineUtils.performRefine(typedef, refineBuilder);
    }
}
