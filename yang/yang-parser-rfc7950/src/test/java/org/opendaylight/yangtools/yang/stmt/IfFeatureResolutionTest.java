/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

public class IfFeatureResolutionTest {

    private static final StatementStreamSource FOO_MODULE = sourceForResource("/if-feature-resolution-test/foo.yang");
    private static final StatementStreamSource BAR_MODULE = sourceForResource("/if-feature-resolution-test/bar.yang");

    @Test
    public void testSomeFeaturesSupported() throws ReactorException {
        final Set<QName> supportedFeatures = ImmutableSet.of(
                QName.create("foo-namespace", "test-feature-1"),
                QName.create("foo-namespace", "test-feature-2"),
                QName.create("foo-namespace", "test-feature-3"),
                QName.create("bar-namespace", "imp-feature"));

        final SchemaContext schemaContext = RFC7950Reactors.defaultReactor().newBuild()
                .addSources(FOO_MODULE, BAR_MODULE)
                .setSupportedFeatures(supportedFeatures)
                .buildEffective();

        final Module testModule = schemaContext.findModule("foo").get();
        assertEquals(9, testModule.getChildNodes().size());

        assertNull(testModule.dataChildByName(QName.create(testModule.getQNameModule(), "test-container-a")));

        final ContainerSchemaNode testContainerB = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-b"));
        final LeafSchemaNode testLeafB = (LeafSchemaNode) testContainerB.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-b"));

        final ContainerSchemaNode testContainerC = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-c"));
        final LeafSchemaNode testLeafC = (LeafSchemaNode) testContainerC.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-c"));

        assertNull(testModule.dataChildByName(QName.create(testModule.getQNameModule(), "test-container-d")));

        final ContainerSchemaNode testContainerE = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-e"));
        final ContainerSchemaNode testSubContainerE = (ContainerSchemaNode) testContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-e"));
        assertNull(testSubContainerE.dataChildByName(QName.create(testModule.getQNameModule(), "test-leaf-e")));

        final ContainerSchemaNode testContainerF = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-f"));
        assertNull(testContainerF.dataChildByName(QName.create(testModule.getQNameModule(), "test-subcontainer-f")));

        final ContainerSchemaNode testContainerG = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-g"));
        assertEquals(1, testContainerG.getAvailableAugmentations().size());
        final LeafSchemaNode testLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-g"));
        assertNull(testContainerG.dataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-leaf-g")));
        final AnyxmlSchemaNode augmentingTestAnyxmlG = (AnyxmlSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-anyxml-g"));

        final ContainerSchemaNode testContainerH = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-h"));
        assertEquals(0, testContainerH.getChildNodes().size());
        assertEquals(0, testContainerH.getUses().size());

        final ContainerSchemaNode testContainerI = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-i"));
        assertEquals(1, testContainerI.getUses().size());
        ContainerSchemaNode testGroupingSubContainer = (ContainerSchemaNode) testContainerI.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNull(testGroupingSubContainer.dataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf")));

        final ContainerSchemaNode testContainerJ = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-j"));
        final LeafSchemaNode testLeafJ = (LeafSchemaNode) testContainerJ.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-j"));

        final ContainerSchemaNode testContainerK = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-k"));
        assertEquals(1, testContainerK.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerK.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertEquals(1, testGroupingSubContainer.getAvailableAugmentations().size());
        final LeafSchemaNode augmentingTestGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf"));
        assertNull(testGroupingSubContainer.dataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf-2")));
    }

    @Test
    public void testAllFeaturesSupported() throws ReactorException {
        final SchemaContext schemaContext = RFC7950Reactors.defaultReactor().newBuild()
                .addSources(FOO_MODULE, BAR_MODULE)
                .buildEffective();

        final Module testModule = schemaContext.findModules("foo").iterator().next();

        assertEquals(11, testModule.getChildNodes().size());

        final ContainerSchemaNode testContainerA = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-a"));
        final LeafSchemaNode testLeafA = (LeafSchemaNode) testContainerA.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-a"));


        final ContainerSchemaNode testContainerB = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-b"));
        final LeafSchemaNode testLeafB = (LeafSchemaNode) testContainerB.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-b"));

        final ContainerSchemaNode testContainerC = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-c"));
        final LeafSchemaNode testLeafC = (LeafSchemaNode) testContainerC.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-c"));

        final ContainerSchemaNode testContainerD = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-d"));
        final LeafSchemaNode testLeafD = (LeafSchemaNode) testContainerD.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-d"));

        final ContainerSchemaNode testContainerE = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-e"));
        final ContainerSchemaNode testSubContainerE = (ContainerSchemaNode) testContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-e"));
        final LeafSchemaNode testLeafE = (LeafSchemaNode) testSubContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-e"));

        final ContainerSchemaNode testContainerF = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-f"));
        final ContainerSchemaNode testSubContainerF = (ContainerSchemaNode) testContainerF.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-f"));
        final ContainerSchemaNode testSubSubContainerF = (ContainerSchemaNode) testSubContainerF.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subsubcontainer-f"));
        final LeafSchemaNode testLeafF = (LeafSchemaNode) testSubSubContainerF.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-f"));

        final ContainerSchemaNode testContainerG = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-g"));
        assertEquals(2, testContainerG.getAvailableAugmentations().size());
        final LeafSchemaNode testLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-g"));
        final LeafSchemaNode augmentingTestLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-leaf-g"));
        final AnyxmlSchemaNode augmentingTestAnyxmlG = (AnyxmlSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-anyxml-g"));

        final ContainerSchemaNode testContainerH = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-h"));
        assertEquals(1, testContainerH.getUses().size());
        ContainerSchemaNode testGroupingSubContainer = (ContainerSchemaNode) testContainerH.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        LeafSchemaNode testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));

        final ContainerSchemaNode testContainerI = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-i"));
        assertEquals(1, testContainerI.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerI.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));

        final ContainerSchemaNode testContainerJ = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-j"));
        final LeafSchemaNode testLeafJ = (LeafSchemaNode) testContainerJ.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-j"));

        final ContainerSchemaNode testContainerK = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-k"));
        assertEquals(1, testContainerK.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerK.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertEquals(1, testGroupingSubContainer.getAvailableAugmentations().size());
        final LeafSchemaNode augmentingTestGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf"));
        final LeafSchemaNode augmentingTestGroupingLeaf2 = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf-2"));
        testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
    }

    @Test
    public void testNoFeaturesSupported() throws ReactorException {
        final SchemaContext schemaContext = RFC7950Reactors.defaultReactor().newBuild()
                .addSources(FOO_MODULE, BAR_MODULE)
                .setSupportedFeatures(ImmutableSet.of())
                .buildEffective();

        final Module testModule = schemaContext.findModules("foo").iterator().next();

        assertEquals(6, testModule.getChildNodes().size());

        final ContainerSchemaNode testContainerE = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-e"));
        final ContainerSchemaNode testSubContainerE = (ContainerSchemaNode) testContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-e"));
        assertNull((LeafSchemaNode) testSubContainerE.dataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-e")));

        final ContainerSchemaNode testContainerF = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-f"));
        assertNull(testContainerF.dataChildByName(QName.create(testModule.getQNameModule(), "test-subcontainer-f")));

        final ContainerSchemaNode testContainerG = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-g"));
        assertEquals(1, testContainerG.getAvailableAugmentations().size());
        final LeafSchemaNode testLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-g"));
        assertNull(testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-leaf-g")));
        final AnyxmlSchemaNode augmentingTestAnyxmlG = (AnyxmlSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-anyxml-g"));
        final ContainerSchemaNode testContainerH = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-h"));
        assertEquals(0, testContainerH.getChildNodes().size());
        assertEquals(0, testContainerH.getUses().size());

        final ContainerSchemaNode testContainerI = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-i"));
        assertEquals(1, testContainerI.getUses().size());
        ContainerSchemaNode testGroupingSubContainer = (ContainerSchemaNode) testContainerI.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNull((LeafSchemaNode) testGroupingSubContainer.dataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf")));

        final ContainerSchemaNode testContainerK = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-k"));
        assertEquals(1, testContainerK.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerK.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertEquals(1, testGroupingSubContainer.getAvailableAugmentations().size());
        assertNull(testGroupingSubContainer.dataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf")));
        final LeafSchemaNode augmentingTestGroupingLeaf2 = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf-2"));
        assertNull(augmentingTestGroupingLeaf2);
        assertNull(testGroupingSubContainer.dataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf")));
    }
}
