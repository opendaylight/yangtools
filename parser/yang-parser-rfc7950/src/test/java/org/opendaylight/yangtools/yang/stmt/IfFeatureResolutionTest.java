/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

class IfFeatureResolutionTest extends AbstractYangTest {
    @Test
    void testSomeFeaturesSupported() {
        final var schemaContext = assertEffectiveModelDir("/if-feature-resolution-test",
            Set.of(
                QName.create("foo-namespace", "test-feature-1"),
                QName.create("foo-namespace", "test-feature-2"),
                QName.create("foo-namespace", "test-feature-3"),
                QName.create("bar-namespace", "imp-feature")));

        final var testModule = schemaContext.findModule("foo").orElseThrow();
        assertEquals(9, testModule.getChildNodes().size());

        final var testContainerA = (ContainerSchemaNode) testModule.dataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-a"));
        assertNull(testContainerA);

        final var testContainerB = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-b"));
        assertNotNull(testContainerB);
        final var testLeafB = (LeafSchemaNode) testContainerB.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-b"));
        assertNotNull(testLeafB);

        final var testContainerC = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-c"));
        assertNotNull(testContainerC);
        final var testLeafC = (LeafSchemaNode) testContainerC.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-c"));
        assertNotNull(testLeafC);

        final var testContainerD = (ContainerSchemaNode) testModule.dataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-d"));
        assertNull(testContainerD);

        final var testContainerE = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-e"));
        assertNotNull(testContainerE);
        final var testSubContainerE = (ContainerSchemaNode) testContainerE.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-subcontainer-e"));
        assertNotNull(testSubContainerE);
        final var testLeafE = (LeafSchemaNode) testSubContainerE.dataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-e"));
        assertNull(testLeafE);

        final var testContainerF = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-f"));
        assertNotNull(testContainerF);
        final var testSubContainerF = (ContainerSchemaNode) testContainerF.dataChildByName(
            QName.create(testModule.getQNameModule(), "test-subcontainer-f"));
        assertNull(testSubContainerF);

        final var testContainerG = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-g"));
        assertNotNull(testContainerG);
        assertEquals(1, testContainerG.getAvailableAugmentations().size());
        final var testLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-g"));
        assertNotNull(testLeafG);
        final var augmentingTestLeafG = (LeafSchemaNode) testContainerG.dataChildByName(
            QName.create(testModule.getQNameModule(), "augmenting-test-leaf-g"));
        assertNull(augmentingTestLeafG);
        final var augmentingTestAnyxmlG = (AnyxmlSchemaNode) testContainerG.getDataChildByName(
            QName.create(testModule.getQNameModule(), "augmenting-test-anyxml-g"));
        assertNotNull(augmentingTestAnyxmlG);

        final var testContainerH = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-h"));
        assertNotNull(testContainerH);
        assertEquals(0, testContainerH.getChildNodes().size());
        assertEquals(0, testContainerH.getUses().size());

        final var testContainerI = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-i"));
        assertNotNull(testContainerI);
        assertEquals(1, testContainerI.getUses().size());
        var testGroupingSubContainer = (ContainerSchemaNode) testContainerI.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        final var testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.dataChildByName(
            QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNull(testGroupingLeaf);

        final var testContainerJ = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-j"));
        assertNotNull(testContainerJ);
        final var testLeafJ = (LeafSchemaNode) testContainerJ.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-j"));
        assertNotNull(testLeafJ);

        final var testContainerK = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-k"));
        assertNotNull(testContainerK);
        assertEquals(1, testContainerK.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerK.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        assertEquals(1, testGroupingSubContainer.getAvailableAugmentations().size());
        final var augmentingTestGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
            QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf"));
        assertNotNull(augmentingTestGroupingLeaf);
        final var augmentingTestGroupingLeaf2 = (LeafSchemaNode) testGroupingSubContainer.dataChildByName(
            QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf-2"));
        assertNull(augmentingTestGroupingLeaf2);
    }

    @Test
    void testAllFeaturesSupported() {
        final var schemaContext = assertEffectiveModelDir("/if-feature-resolution-test");

        final var testModule = schemaContext.findModules("foo").iterator().next();
        assertNotNull(testModule);

        assertEquals(11, testModule.getChildNodes().size());

        final var testContainerA = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-a"));
        assertNotNull(testContainerA);
        final var testLeafA = (LeafSchemaNode) testContainerA.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-a"));
        assertNotNull(testLeafA);

        final var testContainerB = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-b"));
        assertNotNull(testContainerB);
        final var testLeafB = (LeafSchemaNode) testContainerB.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-b"));
        assertNotNull(testLeafB);

        final var testContainerC = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-c"));
        assertNotNull(testContainerC);
        final var testLeafC = (LeafSchemaNode) testContainerC.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-c"));
        assertNotNull(testLeafC);

        final var testContainerD = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-d"));
        assertNotNull(testContainerD);
        final var testLeafD = (LeafSchemaNode) testContainerD.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-d"));
        assertNotNull(testLeafD);

        final var testContainerE = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-e"));
        assertNotNull(testContainerE);
        final var testSubContainerE = (ContainerSchemaNode) testContainerE.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-subcontainer-e"));
        assertNotNull(testSubContainerE);
        final var testLeafE = (LeafSchemaNode) testSubContainerE.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-e"));
        assertNotNull(testLeafE);

        final var testContainerF = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-f"));
        assertNotNull(testContainerF);
        final var testSubContainerF = (ContainerSchemaNode) testContainerF.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-subcontainer-f"));
        assertNotNull(testSubContainerF);
        final var testSubSubContainerF = (ContainerSchemaNode) testSubContainerF.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-subsubcontainer-f"));
        assertNotNull(testSubSubContainerF);
        final var testLeafF = (LeafSchemaNode) testSubSubContainerF.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-f"));
        assertNotNull(testLeafF);

        final var testContainerG = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-g"));
        assertNotNull(testContainerG);
        assertEquals(2, testContainerG.getAvailableAugmentations().size());
        final var testLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-g"));
        assertNotNull(testLeafG);
        final var augmentingTestLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
            QName.create(testModule.getQNameModule(), "augmenting-test-leaf-g"));
        assertNotNull(augmentingTestLeafG);
        final var augmentingTestAnyxmlG = (AnyxmlSchemaNode) testContainerG.getDataChildByName(
            QName.create(testModule.getQNameModule(), "augmenting-test-anyxml-g"));
        assertNotNull(augmentingTestAnyxmlG);

        final var testContainerH = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-h"));
        assertNotNull(testContainerH);
        assertEquals(1, testContainerH.getUses().size());
        var testGroupingSubContainer = (ContainerSchemaNode) testContainerH.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        var testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNotNull(testGroupingLeaf);

        final var testContainerI = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-i"));
        assertNotNull(testContainerI);
        assertEquals(1, testContainerI.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerI.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNotNull(testGroupingLeaf);

        final var testContainerJ = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-j"));
        assertNotNull(testContainerJ);
        final var testLeafJ = (LeafSchemaNode) testContainerJ.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-j"));
        assertNotNull(testLeafJ);

        final var testContainerK = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-k"));
        assertNotNull(testContainerK);
        assertEquals(1, testContainerK.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerK.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        assertEquals(1, testGroupingSubContainer.getAvailableAugmentations().size());
        final var augmentingTestGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
            QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf"));
        assertNotNull(augmentingTestGroupingLeaf);
        final var augmentingTestGroupingLeaf2 = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
            QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf-2"));
        assertNotNull(augmentingTestGroupingLeaf2);
        testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNotNull(testGroupingLeaf);
    }

    @Test
    void testNoFeaturesSupported() {
        final var schemaContext = assertEffectiveModelDir("/if-feature-resolution-test", Set.of());

        final var testModule = schemaContext.findModules("foo").iterator().next();
        assertNotNull(testModule);

        assertEquals(6, testModule.getChildNodes().size());

        final var testContainerE = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-e"));
        assertNotNull(testContainerE);
        final var testSubContainerE = (ContainerSchemaNode) testContainerE.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-subcontainer-e"));
        assertNotNull(testSubContainerE);
        final var testLeafE = (LeafSchemaNode) testSubContainerE.dataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-e"));
        assertNull(testLeafE);

        final var testContainerF = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-f"));
        assertNotNull(testContainerF);
        final var testSubContainerF = (ContainerSchemaNode) testContainerF.dataChildByName(
            QName.create(testModule.getQNameModule(), "test-subcontainer-f"));
        assertNull(testSubContainerF);

        final var testContainerG = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-g"));
        assertNotNull(testContainerG);
        assertEquals(1, testContainerG.getAvailableAugmentations().size());
        final var testLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-leaf-g"));
        assertNotNull(testLeafG);
        final var augmentingTestLeafG = (LeafSchemaNode) testContainerG.dataChildByName(
            QName.create(testModule.getQNameModule(), "augmenting-test-leaf-g"));
        assertNull(augmentingTestLeafG);
        final var augmentingTestAnyxmlG = (AnyxmlSchemaNode) testContainerG.getDataChildByName(
            QName.create(testModule.getQNameModule(), "augmenting-test-anyxml-g"));
        assertNotNull(augmentingTestAnyxmlG);

        final var testContainerH = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-h"));
        assertNotNull(testContainerH);
        assertEquals(0, testContainerH.getChildNodes().size());
        assertEquals(0, testContainerH.getUses().size());

        final var testContainerI = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-i"));
        assertNotNull(testContainerI);
        assertEquals(1, testContainerI.getUses().size());
        var testGroupingSubContainer = (ContainerSchemaNode) testContainerI.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        var testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.dataChildByName(
            QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNull(testGroupingLeaf);

        final var testContainerK = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container-k"));
        assertNotNull(testContainerK);
        assertEquals(1, testContainerK.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerK.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        assertEquals(1, testGroupingSubContainer.getAvailableAugmentations().size());
        final var augmentingTestGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.dataChildByName(
            QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf"));
        assertNull(augmentingTestGroupingLeaf);
        final var augmentingTestGroupingLeaf2 = (LeafSchemaNode) testGroupingSubContainer.dataChildByName(
            QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf-2"));
        assertNull(augmentingTestGroupingLeaf2);
        testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.dataChildByName(
            QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNull(testGroupingLeaf);
    }
}
