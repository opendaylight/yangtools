/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.util.Uint16;
import org.opendaylight.yangtools.yang.parser.builder.api.*;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

/**
 * Test suite for increasing of test coverage of CopyUtils implementation.
 *
 * @see org.opendaylight.yangtools.yang.parser.builder.impl.CopyUtils
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
public class CopyUtilsTest extends AbstractBuilderTest {

    @Test(expected = YangParseException.class)
    public void testCopyOfUnknownTypeOfDataSchemaNode() {
        final String leafLocalName = "leaf-to-refine";
        final QName leafName = QName.create(module.getNamespace(), module.getRevision(), leafLocalName);
        final SchemaPath leafPath = SchemaPath.create(true, leafName);
        final LeafSchemaNodeBuilder leafBuilder = new LeafSchemaNodeBuilder(module.getModuleName(), 22, leafName, leafPath);

        CopyUtils.copy(new InvalidDataSchemaNodeBuilder(), leafBuilder, false);
    }

    @Test
    public void testCopyAnyxmlWithAnyxmlOriginalNodeAndUnknownNodes() {
        final String parentAnyxmlLocalName = "original-anyxml";
        final QName parentAnyxmlName = QName.create(module.getNamespace(), module.getRevision(), parentAnyxmlLocalName);
        final SchemaPath parentAnyxmlPath = SchemaPath.create(true, parentAnyxmlName);
        final AnyXmlBuilder parentAnyXmlBuilder = new AnyXmlBuilder(module.getModuleName(), 22, parentAnyxmlName, parentAnyxmlPath);

        final String anyxmlLocalName = "anyxml";
        final QName anyxmlName = QName.create(module.getNamespace(), module.getRevision(), anyxmlLocalName);
        final SchemaPath anyxmlPath = SchemaPath.create(true, anyxmlName);
        final AnyXmlBuilder anyXmlBuilder = new AnyXmlBuilder(module.getModuleName(), 22, anyxmlName, anyxmlPath);

        anyXmlBuilder.setOriginal(parentAnyXmlBuilder);

        final UnknownSchemaNodeBuilder unknownSchemaNodeBuilder = provideUnknownNodeBuilder(anyxmlName);
        anyXmlBuilder.addUnknownNodeBuilder(unknownSchemaNodeBuilder);

        final AnyXmlBuilder copy = (AnyXmlBuilder) CopyUtils.copy(anyXmlBuilder, anyXmlBuilder, false);
        assertFalse(copy.getUnknownNodes().isEmpty());
        assertNotNull(copy.getOriginal());
        assertEquals(copy.getOriginal(), parentAnyXmlBuilder);
    }

    @Test
    public void testCopyChoiceBuilderWithUnknownNodesAndAugmentation() {
        final String originalChoiceLocalName = "original-choice-to-copy";
        final ChoiceBuilder originalChoiceBuilder = provideChoiceBuilder(originalChoiceLocalName);

        final String choiceLocalName = "choice-to-copy";
        final ChoiceBuilder choiceBuilder = provideChoiceBuilder(choiceLocalName);
        choiceBuilder.setOriginal(originalChoiceBuilder);

        final UnknownSchemaNodeBuilder unknownSchemaNodeBuilder = provideUnknownNodeBuilder(choiceBuilder.getQName());

        final QName choiceName = QName.create(module.getNamespace(), module.getRevision(), choiceLocalName);
        final SchemaPath augPath = SchemaPath.create(true, choiceName);

        final AugmentationSchemaBuilder augBuilder = new AugmentationSchemaBuilderImpl(module.getModuleName(), 22,
            "/imaginary/path", augPath, 0);

        final UsesNodeBuilder usesNodeBuilder = provideUsesNodeBuilder("test-grouping-use");
            augBuilder.addUsesNode(usesNodeBuilder);
        augBuilder.addUnknownNodeBuilder(unknownSchemaNodeBuilder);

        choiceBuilder.addUnknownNodeBuilder(unknownSchemaNodeBuilder);
        choiceBuilder.addAugmentation(augBuilder);

        final ChoiceBuilder copy = (ChoiceBuilder)CopyUtils.copy(choiceBuilder, module, false);
        List<AugmentationSchemaBuilder> augmentations = copy.getAugmentationBuilders();

        assertFalse(copy.getUnknownNodes().isEmpty());

        final UnknownSchemaNodeBuilder copyUnknownNode = copy.getUnknownNodes().get(0);
        assertEquals(copyUnknownNode, unknownSchemaNodeBuilder);

        assertFalse(augmentations.isEmpty());
        final AugmentationSchemaBuilder copyAugBuilder = augmentations.get(0);

        assertEquals(copyAugBuilder, augBuilder);
        assertEquals(copyAugBuilder.getUnknownNodes().get(0), augBuilder.getUnknownNodes().get(0));
        assertNotEquals(copyAugBuilder.getUsesNodeBuilders().get(0), augBuilder.getUsesNodeBuilders().get(0));
    }

    @Test
    public void testCopyChoiceCaseBuilder() {
        final String originalChoiceCaseLocalName = "original-choice-case";
        final QName originalChoiceCaseQName = QName.create(module.getNamespace(), module.getRevision(),
            originalChoiceCaseLocalName);
        final SchemaPath originalChoiceCasePath = SchemaPath.create(true, originalChoiceCaseQName);
        final ChoiceCaseBuilder originalChoiceCaseBuilder = new ChoiceCaseBuilder(module.getModuleName(), 10,
            originalChoiceCaseQName, originalChoiceCasePath);

        final String choiceCaseLocalName = "test-choice-case";
        final QName choiceCaseQName = QName.create(module.getNamespace(), module.getRevision(), choiceCaseLocalName);
        final SchemaPath choiceCasePath = SchemaPath.create(true, choiceCaseQName);
        final ChoiceCaseBuilder choiceCaseBuilder = new ChoiceCaseBuilder(module.getModuleName(), 10, choiceCaseQName,
            choiceCasePath);

        choiceCaseBuilder.setOriginal(originalChoiceCaseBuilder);

        final String groupLocalName = "test-group";
        final QName testGroup = QName.create(module.getNamespace(), module.getRevision(), groupLocalName);
        final SchemaPath groupPath = SchemaPath.create(true, choiceCaseQName, testGroup);
        final GroupingBuilder grouping = module.addGrouping(12, testGroup, groupPath);

        final UsesNodeBuilder usesNodeBuilder = provideUsesNodeBuilder("test-grouping-use");

        UnknownSchemaNodeBuilder unknownSchemaNodeBuilder = provideUnknownNodeBuilder(choiceCaseQName);

        choiceCaseBuilder.addGrouping(grouping);
        choiceCaseBuilder.addUsesNode(usesNodeBuilder);
        choiceCaseBuilder.addUnknownNodeBuilder(unknownSchemaNodeBuilder);

        ChoiceCaseBuilder copy = (ChoiceCaseBuilder) CopyUtils.copy(choiceCaseBuilder, module, true);

        assertNotNull(copy);
        assertFalse(copy.getGroupingBuilders().isEmpty());

    }

    @Test
    public void testCopyContainerSchemaNodeBuilder() {
        final ContainerSchemaNodeBuilder containerBuilder = provideContainerBuilder("parent-container");

        final String groupLocalName = "test-group";
        final QName testGroup = QName.create(module.getNamespace(), module.getRevision(), groupLocalName);
        final SchemaPath groupPath = SchemaPath.create(true, containerBuilder.getQName(), testGroup);
        final GroupingBuilder grouping = module.addGrouping(12, testGroup, groupPath);

        final String typedefLocalName = "test-type-definition";
        final QName typedefQname = QName.create(module.getNamespace(), module.getRevision(), typedefLocalName);
        final SchemaPath typedefPath = SchemaPath.create(true, containerBuilder.getQName(), typedefQname);
        final TypeDefinitionBuilder typedefBuilder = new TypeDefinitionBuilderImpl(module.getModuleName(), 12,
            typedefQname, typedefPath);
        typedefBuilder.setType(Uint16.getInstance());

        final UnknownSchemaNodeBuilder unkownNodeBuilder = provideUnknownNodeBuilder(containerBuilder.getQName());

        containerBuilder.addGrouping(grouping);
        containerBuilder.addTypedef(typedefBuilder);
        containerBuilder.addUnknownNodeBuilder(unkownNodeBuilder);

        ContainerSchemaNodeBuilder copy = (ContainerSchemaNodeBuilder) CopyUtils.copy(containerBuilder, module, true);

        assertNotNull(copy);
    }

    @Test
    public void testCopyLeafSchemaNodeBuilder() {

    }

    @Test
    public void testCopyLeafListSchemaNodeBuilder() {

    }

    @Test
    public void testCopyListSchemaNodeBuilder() {

    }

    @Test
    public void testCopyGroupingBuilder() {

    }

    @Test
    public void testCopyTypeDefinitionBuilder() {

    }

    private static final class InvalidDataSchemaNodeBuilder implements DataSchemaNodeBuilder {

        @Override public SchemaNodeBuilder getOriginal() {
            return null;
        }

        @Override public void setOriginal(SchemaNodeBuilder original) {

        }

        @Override public boolean isAugmenting() {
            return false;
        }

        @Override public void setAugmenting(boolean augmenting) {

        }

        @Override public boolean isConfiguration() {
            return false;
        }

        @Override public void setConfiguration(boolean config) {

        }

        @Override public ConstraintsBuilder getConstraints() {
            return null;
        }

        @Override public QName getQName() {
            return null;
        }

        @Override public SchemaPath getPath() {
            return null;
        }

        @Override public void setPath(SchemaPath path) {

        }

        @Override public String getModuleName() {
            return null;
        }

        @Override public void setModuleName(String moduleName) {

        }

        @Override public int getLine() {
            return 0;
        }

        @Override public Builder getParent() {
            return null;
        }

        @Override public void setParent(Builder parent) {

        }

        @Override public void addUnknownNodeBuilder(UnknownSchemaNodeBuilder unknownNode) {

        }

        @Override public List<UnknownSchemaNodeBuilder> getUnknownNodes() {
            return null;
        }

        @Override public DataSchemaNode build() {
            return null;
        }

        @Override public String getDescription() {
            return null;
        }

        @Override public void setDescription(String description) {

        }

        @Override public String getReference() {
            return null;
        }

        @Override public void setReference(String reference) {

        }

        @Override public Status getStatus() {
            return null;
        }

        @Override public void setStatus(Status status) {

        }

        @Override public boolean isAddedByUses() {
            return false;
        }

        @Override public void setAddedByUses(boolean addedByUses) {

        }
    }
}
