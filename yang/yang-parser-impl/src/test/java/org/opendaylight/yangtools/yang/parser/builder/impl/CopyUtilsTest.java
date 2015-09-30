/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.util.Uint16;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.ConstraintsBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

/**
 * Test suite for increasing of test coverage of CopyUtils implementation.
 *
 * @see org.opendaylight.yangtools.yang.parser.builder.impl.CopyUtils
 *
 * @author Lukas Sedlak &lt;lsedlak@cisco.com&gt;
 *
 * @deprecated Pre-Beryllium implementation, scheduled for removal.
 */
@Deprecated
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
        final SchemaPath augPath = SchemaPath.create(true, choiceBuilder.getQName());

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

        final UsesNodeBuilder usesNodeBuilder = provideUsesNodeBuilder("test-grouping-use");

        UnknownSchemaNodeBuilder unknownSchemaNodeBuilder = provideUnknownNodeBuilder(choiceCaseQName);

        choiceCaseBuilder.addUsesNode(usesNodeBuilder);
        choiceCaseBuilder.addUnknownNodeBuilder(unknownSchemaNodeBuilder);

        ChoiceCaseBuilder copy = (ChoiceCaseBuilder) CopyUtils.copy(choiceCaseBuilder, module, true);

        assertNotNull(copy);
        assertEquals(copy.getUnknownNodes().get(0), choiceCaseBuilder.getUnknownNodes().get(0));
        assertFalse(copy.getUsesNodeBuilders().isEmpty());
        assertNotEquals(copy.getUsesNodeBuilders().get(0), choiceCaseBuilder.getUsesNodeBuilders().get(0));
    }

    @Test(expected = YangParseException.class)
    public void testAddGroupingIntoChoiceCaseBuilder() {
        final String choiceCaseLocalName = "test-choice-case";
        final QName choiceCaseQName = QName.create(module.getNamespace(), module.getRevision(), choiceCaseLocalName);
        final SchemaPath choiceCasePath = SchemaPath.create(true, choiceCaseQName);
        final ChoiceCaseBuilder choiceCaseBuilder = new ChoiceCaseBuilder(module.getModuleName(), 10, choiceCaseQName,
            choiceCasePath);

        final String groupLocalName = "test-group";
        final QName testGroup = QName.create(module.getNamespace(), module.getRevision(), groupLocalName);
        final SchemaPath groupPath = SchemaPath.create(true, choiceCaseQName, testGroup);
        final GroupingBuilder grouping = module.addGrouping(12, testGroup, groupPath);
        choiceCaseBuilder.addGrouping(grouping);
    }

    @Test
    public void testCopyContainerSchemaNodeBuilder() {
        final ContainerSchemaNodeBuilder containerBuilder = provideContainerBuilder("parent-container");

        final String groupLocalName = "test-group";
        final GroupingBuilder grouping = provideNestedGroupingDefinition(containerBuilder.getQName(), groupLocalName);

        final String typedefLocalName = "test-type-definition";
        final TypeDefinitionBuilder typedefBuilder = provideNestedTypedef(containerBuilder.getQName(), typedefLocalName);

        final UnknownSchemaNodeBuilder unknownNodeBuilder = provideUnknownNodeBuilder(containerBuilder.getQName());

        containerBuilder.addGrouping(grouping);
        containerBuilder.addTypedef(typedefBuilder);
        containerBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        final ContainerSchemaNodeBuilder copy = (ContainerSchemaNodeBuilder) CopyUtils.copy(containerBuilder, module, true);

        assertNotNull(copy);

        assertFalse(copy.getGroupingBuilders().isEmpty());
        assertFalse(copy.getTypeDefinitionBuilders().isEmpty());
        assertFalse(copy.getUnknownNodes().isEmpty());
    }

    private GroupingBuilder provideNestedGroupingDefinition(final QName parentName, final String groupLocalName) {
        final QName testGroup = QName.create(module.getNamespace(), module.getRevision(), groupLocalName);
        final SchemaPath groupPath = SchemaPath.create(true, parentName, testGroup);
        final GroupingBuilder grouping = module.addGrouping(12, testGroup, groupPath);

        return grouping;
    }

    private TypeDefinitionBuilder provideNestedTypedef(final QName parentName, final String typedefLocalName) {
        final QName typedefQname = QName.create(module.getNamespace(), module.getRevision(), typedefLocalName);
        final SchemaPath typedefPath = SchemaPath.create(true, parentName, typedefQname);
        final TypeDefinitionBuilder typedefBuilder = new TypeDefinitionBuilderImpl(module.getModuleName(), 12,
            typedefQname, typedefPath);
        typedefBuilder.setType(Uint16.getInstance());

        return typedefBuilder;
    }

    @Test
    public void testCopyLeafSchemaNodeBuilder() {
        final String leafLocalName = "original-leaf";
        final QName leafName = QName.create(module.getNamespace(), module.getRevision(), leafLocalName);
        final SchemaPath leafPath = SchemaPath.create(true, leafName);
        final LeafSchemaNodeBuilder leafBuilder = new LeafSchemaNodeBuilder(module.getModuleName(), 22, leafName, leafPath);
        leafBuilder.setType(Uint16.getInstance());

        final UnknownSchemaNodeBuilder unknownNodeBuilder = provideUnknownNodeBuilder(leafName);
        leafBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        final LeafSchemaNodeBuilder copy = (LeafSchemaNodeBuilder)CopyUtils.copy(leafBuilder, module, true);

        assertNotNull(copy);
        assertFalse(leafBuilder.getUnknownNodes().isEmpty());
    }

    @Test
    public void testCopyLeafListSchemaNodeBuilder() {
        final String origLeafListLocalName = "original-list-to";
        final QName origLeafListQName = QName.create(module.getNamespace(), module.getRevision(), origLeafListLocalName );
        final SchemaPath origLeafListPath = SchemaPath.create(true, origLeafListQName);
        final LeafListSchemaNodeBuilder origLeafListBuilder = new LeafListSchemaNodeBuilder(module.getModuleName(),
            10, origLeafListQName, origLeafListPath);
        origLeafListBuilder.setType(Uint16.getInstance());

        final String leafListLocalName = "leaflist-copy";
        final QName leafListQName = QName.create(module.getNamespace(), module.getRevision(), leafListLocalName);
        final SchemaPath leafListPath = SchemaPath.create(true, leafListQName);
        final LeafListSchemaNodeBuilder leafListBuilder = new LeafListSchemaNodeBuilder(module.getModuleName(),
            20, leafListQName, leafListPath);
        leafListBuilder.setType(Uint16.getInstance());

        leafListBuilder.setOriginal(origLeafListBuilder);
        final UnknownSchemaNodeBuilder unknownNodeBuilder = provideUnknownNodeBuilder(leafListQName);
        leafListBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        final LeafListSchemaNodeBuilder copy = (LeafListSchemaNodeBuilder)CopyUtils.copy(leafListBuilder, module, true);

        assertNotNull(copy);
        assertFalse(copy.getUnknownNodes().isEmpty());
        assertNotNull(copy.getUnknownNodes().get(0));
    }

    @Test
    public void testCopyListSchemaNodeBuilder() {
        final String origListLocalName = "original-list";
        final QName origListQName = QName.create(module.getNamespace(), module.getRevision(), origListLocalName);
        final SchemaPath origListPath = SchemaPath.create(true, origListQName);
        final ListSchemaNodeBuilder origListBuilder = new ListSchemaNodeBuilder(module.getModuleName(),
            10, origListQName, origListPath);

        final String listLocalName = "copy-of-list";
        final QName listQName = QName.create(module.getNamespace(), module.getRevision(), listLocalName);
        final SchemaPath listPath = SchemaPath.create(true, listQName);
        final ListSchemaNodeBuilder listBuilder = new ListSchemaNodeBuilder(module.getModuleName(),
            20, listQName, listPath);

        listBuilder.setOriginal(origListBuilder);

        final String groupLocalName = "test-group";
        final GroupingBuilder grouping = provideNestedGroupingDefinition(listBuilder.getQName(), groupLocalName);
        listBuilder.addGrouping(grouping);

        final String typedefLocalName = "test-type-definition";
        final TypeDefinitionBuilder typedefBuilder = provideNestedTypedef(listBuilder.getQName(), typedefLocalName);
        listBuilder.addTypedef(typedefBuilder);

        final UnknownSchemaNodeBuilder unknownNodeBuilder = provideUnknownNodeBuilder(listBuilder.getQName());
        listBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        final UsesNodeBuilder usesNodeBuilder = provideUsesNodeBuilder("test-grouping-use");
        listBuilder.addUsesNode(usesNodeBuilder);

        final SchemaPath augPath = SchemaPath.create(true, listBuilder.getQName());
        final AugmentationSchemaBuilder augBuilder = new AugmentationSchemaBuilderImpl(module.getModuleName(), 22,
            "/imaginary/path", augPath, 0);
        listBuilder.addAugmentation(augBuilder);

        ListSchemaNodeBuilder copy = (ListSchemaNodeBuilder) CopyUtils.copy(listBuilder, module, true);
        assertNotNull(copy);
        assertFalse(copy.getGroupingBuilders().isEmpty());
        assertFalse(copy.getTypeDefinitionBuilders().isEmpty());
        assertFalse(copy.getUnknownNodes().isEmpty());
        assertFalse(copy.getUsesNodeBuilders().isEmpty());
        assertFalse(copy.getAugmentationBuilders().isEmpty());
    }

    @Test
    public void testCopyGroupingBuilder() {
        final String groupLocalName = "test-group";
        final QName testGroup = QName.create(module.getNamespace(), module.getRevision(), groupLocalName);
        final SchemaPath groupPath = SchemaPath.create(true, testGroup);
        final GroupingBuilder grouping = module.addGrouping(12, testGroup, groupPath);

        final String innerGroupLocalName = "inner-group";
        final QName innerGroup = QName.create(module.getNamespace(), module.getRevision(), innerGroupLocalName);
        final SchemaPath innerGroupPath = SchemaPath.create(true, testGroup, innerGroup);
        final GroupingBuilder innerGrouping = module.addGrouping(12, innerGroup, innerGroupPath);

        grouping.addGrouping(innerGrouping);

        final String typedefLocalName = "test-type-definition";
        final TypeDefinitionBuilder typedefBuilder = provideNestedTypedef(grouping.getQName(), typedefLocalName);
        grouping.addTypedef(typedefBuilder);

        final UsesNodeBuilder usesNodeBuilder = provideUsesNodeBuilder(innerGroupLocalName);
        grouping.addUsesNode(usesNodeBuilder);

        final UnknownSchemaNodeBuilder unknownNodeBuilder = provideUnknownNodeBuilder(grouping.getQName());
        grouping.addUnknownNodeBuilder(unknownNodeBuilder);

        final GroupingBuilder copy = CopyUtils.copy(grouping, module, true);
        assertNotNull(copy);
        assertFalse(copy.getGroupingBuilders().isEmpty());
        assertFalse(copy.getTypeDefinitionBuilders().isEmpty());
        assertFalse(copy.getUsesNodeBuilders().isEmpty());
        assertFalse(copy.getUnknownNodes().isEmpty());
    }

    @Test
    public void testCopyIdentityrefTypeBuilder() {
        final String typedefLocalName = "identity-ref-test-type";
        final QName typedefQname = QName.create(module.getNamespace(), module.getRevision(), typedefLocalName);
        final SchemaPath typedefPath = SchemaPath.create(true, typedefQname);
        final IdentityrefTypeBuilder typeBuilder = new IdentityrefTypeBuilder(module.getModuleName(), 12,
            "base:parent-identity", typedefPath);

        final TypeDefinitionBuilder copy = CopyUtils.copy(typeBuilder, module, true);
        assertNotNull(copy);

        //TODO: add additional asserts
    }

    @Test
    public void testCopyTypeDefinitionBuilderWithUnknownNodes() {
        final String typedefLocalName = "test-typedef-with-ext";
        final QName typedefQname = QName.create(module.getNamespace(), module.getRevision(), typedefLocalName);
        final SchemaPath typedefPath = SchemaPath.create(true, typedefQname);
        final TypeDefinitionBuilder typedefBuilder = new TypeDefinitionBuilderImpl(module.getModuleName(), 12,
            typedefQname, typedefPath);
        typedefBuilder.setType(Uint16.getInstance());

        typedefBuilder.addUnknownNodeBuilder(provideUnknownNodeBuilder());

        TypeDefinitionBuilder copy = CopyUtils.copy(typedefBuilder, module, true);
        assertNotNull(copy);
        assertFalse(copy.getUnknownNodes().isEmpty());
    }

    // FIXME: Use Mockito instead of this monstrosity
    private static final class InvalidDataSchemaNodeBuilder implements DataSchemaNodeBuilder {

        @Override
        public SchemaNodeBuilder getOriginal() {
            return null;
        }

        @Override
        public void setOriginal(final SchemaNodeBuilder original) {

        }

        @Override
        public boolean isAugmenting() {
            return false;
        }

        @Override
        public void setAugmenting(final boolean augmenting) {

        }

        @Override
        public boolean isConfiguration() {
            return false;
        }

        @Override
        public void setConfiguration(final boolean config) {

        }

        @Override
        public ConstraintsBuilder getConstraints() {
            return null;
        }

        @Override
        public QName getQName() {
            return null;
        }

        @Override
        public SchemaPath getPath() {
            return null;
        }

        @Override
        public void setPath(final SchemaPath path) {

        }

        @Override
        public String getModuleName() {
            return null;
        }

        @Override
        public int getLine() {
            return 0;
        }

        @Override
        public Builder getParent() {
            return null;
        }

        @Override
        public void setParent(final Builder parent) {

        }

        @Override
        public void addUnknownNodeBuilder(final UnknownSchemaNodeBuilder unknownNode) {

        }

        @Override
        public List<UnknownSchemaNodeBuilder> getUnknownNodes() {
            return null;
        }

        @Override
        public DataSchemaNode build() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public void setDescription(final String description) {

        }

        @Override
        public String getReference() {
            return null;
        }

        @Override
        public void setReference(final String reference) {

        }

        @Override
        public Status getStatus() {
            return null;
        }

        @Override
        public void setStatus(final Status status) {

        }

        @Override
        public boolean isAddedByUses() {
            return false;
        }

        @Override
        public void setAddedByUses(final boolean addedByUses) {

        }
    }
}
