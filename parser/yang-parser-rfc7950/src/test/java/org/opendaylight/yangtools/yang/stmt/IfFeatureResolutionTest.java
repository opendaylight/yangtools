/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;

public class IfFeatureResolutionTest {
    @Test
    public void testSomeFeaturesSupported() throws Exception {
        final var schemaContext = StmtTestUtils.parseYangSources("/if-feature-resolution-test",
            Set.of(
                QName.create("foo-namespace", "test-feature-1"),
                QName.create("foo-namespace", "test-feature-2"),
                QName.create("foo-namespace", "test-feature-3"),
                QName.create("bar-namespace", "imp-feature")),
            YangParserConfiguration.DEFAULT);

        final Module testModule = schemaContext.findModule("foo").get();
        assertEquals(9, testModule.getChildNodes().size());

        final ContainerSchemaNode testContainerA = (ContainerSchemaNode) testModule.dataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-a"));
        assertNull(testContainerA);

        final ContainerSchemaNode testContainerB = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-b"));
        assertNotNull(testContainerB);
        final LeafSchemaNode testLeafB = (LeafSchemaNode) testContainerB.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-b"));
        assertNotNull(testLeafB);

        final ContainerSchemaNode testContainerC = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-c"));
        assertNotNull(testContainerC);
        final LeafSchemaNode testLeafC = (LeafSchemaNode) testContainerC.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-c"));
        assertNotNull(testLeafC);

        final ContainerSchemaNode testContainerD = (ContainerSchemaNode) testModule.dataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-d"));
        assertNull(testContainerD);

        final ContainerSchemaNode testContainerE = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-e"));
        assertNotNull(testContainerE);
        final ContainerSchemaNode testSubContainerE = (ContainerSchemaNode) testContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-e"));
        assertNotNull(testSubContainerE);
        final LeafSchemaNode testLeafE = (LeafSchemaNode) testSubContainerE.dataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-e"));
        assertNull(testLeafE);

        final ContainerSchemaNode testContainerF = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-f"));
        assertNotNull(testContainerF);
        final ContainerSchemaNode testSubContainerF = (ContainerSchemaNode) testContainerF.dataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-f"));
        assertNull(testSubContainerF);

        final ContainerSchemaNode testContainerG = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-g"));
        assertNotNull(testContainerG);
        assertEquals(1, testContainerG.getAvailableAugmentations().size());
        final LeafSchemaNode testLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-g"));
        assertNotNull(testLeafG);
        final LeafSchemaNode augmentingTestLeafG = (LeafSchemaNode) testContainerG.dataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-leaf-g"));
        assertNull(augmentingTestLeafG);
        final AnyxmlSchemaNode augmentingTestAnyxmlG = (AnyxmlSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-anyxml-g"));
        assertNotNull(augmentingTestAnyxmlG);

        final ContainerSchemaNode testContainerH = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-h"));
        assertNotNull(testContainerH);
        assertEquals(0, testContainerH.getChildNodes().size());
        assertEquals(0, testContainerH.getUses().size());

        final ContainerSchemaNode testContainerI = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-i"));
        assertNotNull(testContainerI);
        assertEquals(1, testContainerI.getUses().size());
        ContainerSchemaNode testGroupingSubContainer = (ContainerSchemaNode) testContainerI.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        final LeafSchemaNode testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.dataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNull(testGroupingLeaf);

        final ContainerSchemaNode testContainerJ = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-j"));
        assertNotNull(testContainerJ);
        final LeafSchemaNode testLeafJ = (LeafSchemaNode) testContainerJ.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-j"));
        assertNotNull(testLeafJ);

        final ContainerSchemaNode testContainerK = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-k"));
        assertNotNull(testContainerK);
        assertEquals(1, testContainerK.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerK.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        assertEquals(1, testGroupingSubContainer.getAvailableAugmentations().size());
        final LeafSchemaNode augmentingTestGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf"));
        assertNotNull(augmentingTestGroupingLeaf);
        final LeafSchemaNode augmentingTestGroupingLeaf2 = (LeafSchemaNode) testGroupingSubContainer.dataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf-2"));
        assertNull(augmentingTestGroupingLeaf2);
    }

    @Test
    public void testAllFeaturesSupported() throws Exception {
        final var schemaContext = StmtTestUtils.parseYangSources("/if-feature-resolution-test",
                YangParserConfiguration.DEFAULT);

        final Module testModule = schemaContext.findModules("foo").iterator().next();
        assertNotNull(testModule);

        assertEquals(11, testModule.getChildNodes().size());

        final ContainerSchemaNode testContainerA = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-a"));
        assertNotNull(testContainerA);
        final LeafSchemaNode testLeafA = (LeafSchemaNode) testContainerA.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-a"));
        assertNotNull(testLeafA);


        final ContainerSchemaNode testContainerB = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-b"));
        assertNotNull(testContainerB);
        final LeafSchemaNode testLeafB = (LeafSchemaNode) testContainerB.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-b"));
        assertNotNull(testLeafB);

        final ContainerSchemaNode testContainerC = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-c"));
        assertNotNull(testContainerC);
        final LeafSchemaNode testLeafC = (LeafSchemaNode) testContainerC.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-c"));
        assertNotNull(testLeafC);

        final ContainerSchemaNode testContainerD = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-d"));
        assertNotNull(testContainerD);
        final LeafSchemaNode testLeafD = (LeafSchemaNode) testContainerD.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-d"));
        assertNotNull(testLeafD);

        final ContainerSchemaNode testContainerE = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-e"));
        assertNotNull(testContainerE);
        final ContainerSchemaNode testSubContainerE = (ContainerSchemaNode) testContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-e"));
        assertNotNull(testSubContainerE);
        final LeafSchemaNode testLeafE = (LeafSchemaNode) testSubContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-e"));
        assertNotNull(testLeafE);

        final ContainerSchemaNode testContainerF = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-f"));
        assertNotNull(testContainerF);
        final ContainerSchemaNode testSubContainerF = (ContainerSchemaNode) testContainerF.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-f"));
        assertNotNull(testSubContainerF);
        final ContainerSchemaNode testSubSubContainerF = (ContainerSchemaNode) testSubContainerF.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subsubcontainer-f"));
        assertNotNull(testSubSubContainerF);
        final LeafSchemaNode testLeafF = (LeafSchemaNode) testSubSubContainerF.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-f"));
        assertNotNull(testLeafF);

        final ContainerSchemaNode testContainerG = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-g"));
        assertNotNull(testContainerG);
        assertEquals(2, testContainerG.getAvailableAugmentations().size());
        final LeafSchemaNode testLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-g"));
        assertNotNull(testLeafG);
        final LeafSchemaNode augmentingTestLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-leaf-g"));
        assertNotNull(augmentingTestLeafG);
        final AnyxmlSchemaNode augmentingTestAnyxmlG = (AnyxmlSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-anyxml-g"));
        assertNotNull(augmentingTestAnyxmlG);

        final ContainerSchemaNode testContainerH = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-h"));
        assertNotNull(testContainerH);
        assertEquals(1, testContainerH.getUses().size());
        ContainerSchemaNode testGroupingSubContainer = (ContainerSchemaNode) testContainerH.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        LeafSchemaNode testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNotNull(testGroupingLeaf);

        final ContainerSchemaNode testContainerI = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-i"));
        assertNotNull(testContainerI);
        assertEquals(1, testContainerI.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerI.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNotNull(testGroupingLeaf);

        final ContainerSchemaNode testContainerJ = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-j"));
        assertNotNull(testContainerJ);
        final LeafSchemaNode testLeafJ = (LeafSchemaNode) testContainerJ.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-j"));
        assertNotNull(testLeafJ);

        final ContainerSchemaNode testContainerK = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-k"));
        assertNotNull(testContainerK);
        assertEquals(1, testContainerK.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerK.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        assertEquals(1, testGroupingSubContainer.getAvailableAugmentations().size());
        final LeafSchemaNode augmentingTestGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf"));
        assertNotNull(augmentingTestGroupingLeaf);
        final LeafSchemaNode augmentingTestGroupingLeaf2 = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf-2"));
        assertNotNull(augmentingTestGroupingLeaf2);
        testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNotNull(testGroupingLeaf);
    }

    @Test
    public void testNoFeaturesSupported() throws Exception {
        final var schemaContext = StmtTestUtils.parseYangSources("/if-feature-resolution-test",
            Set.of(), YangParserConfiguration.DEFAULT);

        final Module testModule = schemaContext.findModules("foo").iterator().next();
        assertNotNull(testModule);

        assertEquals(6, testModule.getChildNodes().size());

        final ContainerSchemaNode testContainerE = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-e"));
        assertNotNull(testContainerE);
        final ContainerSchemaNode testSubContainerE = (ContainerSchemaNode) testContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-e"));
        assertNotNull(testSubContainerE);
        final LeafSchemaNode testLeafE = (LeafSchemaNode) testSubContainerE.dataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-e"));
        assertNull(testLeafE);

        final ContainerSchemaNode testContainerF = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-f"));
        assertNotNull(testContainerF);
        final ContainerSchemaNode testSubContainerF = (ContainerSchemaNode) testContainerF.dataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-f"));
        assertNull(testSubContainerF);

        final ContainerSchemaNode testContainerG = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-g"));
        assertNotNull(testContainerG);
        assertEquals(1, testContainerG.getAvailableAugmentations().size());
        final LeafSchemaNode testLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-g"));
        assertNotNull(testLeafG);
        final LeafSchemaNode augmentingTestLeafG = (LeafSchemaNode) testContainerG.dataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-leaf-g"));
        assertNull(augmentingTestLeafG);
        final AnyxmlSchemaNode augmentingTestAnyxmlG = (AnyxmlSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-anyxml-g"));
        assertNotNull(augmentingTestAnyxmlG);

        final ContainerSchemaNode testContainerH = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-h"));
        assertNotNull(testContainerH);
        assertEquals(0, testContainerH.getChildNodes().size());
        assertEquals(0, testContainerH.getUses().size());

        final ContainerSchemaNode testContainerI = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-i"));
        assertNotNull(testContainerI);
        assertEquals(1, testContainerI.getUses().size());
        ContainerSchemaNode testGroupingSubContainer = (ContainerSchemaNode) testContainerI.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        LeafSchemaNode testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.dataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNull(testGroupingLeaf);

        final ContainerSchemaNode testContainerK = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-k"));
        assertNotNull(testContainerK);
        assertEquals(1, testContainerK.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerK.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        assertEquals(1, testGroupingSubContainer.getAvailableAugmentations().size());
        final LeafSchemaNode augmentingTestGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.dataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf"));
        assertNull(augmentingTestGroupingLeaf);
        final LeafSchemaNode augmentingTestGroupingLeaf2 = (LeafSchemaNode) testGroupingSubContainer.dataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf-2"));
        assertNull(augmentingTestGroupingLeaf2);
        testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.dataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNull(testGroupingLeaf);
    }
}
