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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class IfFeatureResolutionTest {

    private static final StatementStreamSource FOO_MODULE =
            new YangStatementSourceImpl("/if-feature-resolution-test/foo.yang", false);
    private static final StatementStreamSource BAR_MODULE =
            new YangStatementSourceImpl("/if-feature-resolution-test/bar.yang", false);

    @Test
    public void testSomeFeaturesSupported() throws ReactorException {
        Predicate<QName> isFeatureSupported = qName -> {
            Set<QName> supportedFeatures = new HashSet<>();
            supportedFeatures.add(QName.create("foo-namespace", "1970-01-01", "test-feature-1"));
            supportedFeatures.add(QName.create("foo-namespace", "1970-01-01", "test-feature-2"));
            supportedFeatures.add(QName.create("foo-namespace", "1970-01-01", "test-feature-3"));
            supportedFeatures.add(QName.create("bar-namespace", "1970-01-01", "imp-feature"));

            return supportedFeatures.contains(qName);
        };

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild(isFeatureSupported);
        reactor.addSources(FOO_MODULE, BAR_MODULE);

        SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);

        Module testModule = schemaContext.findModuleByName("foo", null);
        assertNotNull(testModule);

        assertEquals(9, testModule.getChildNodes().size());

        ContainerSchemaNode testContainerA = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-a"));
        assertNull(testContainerA);

        ContainerSchemaNode testContainerB = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-b"));
        assertNotNull(testContainerB);
        LeafSchemaNode testLeafB = (LeafSchemaNode) testContainerB.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-b"));
        assertNotNull(testLeafB);

        ContainerSchemaNode testContainerC = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-c"));
        assertNotNull(testContainerC);
        LeafSchemaNode testLeafC = (LeafSchemaNode) testContainerC.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-c"));
        assertNotNull(testLeafC);

        ContainerSchemaNode testContainerD = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-d"));
        assertNull(testContainerD);

        ContainerSchemaNode testContainerE = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-e"));
        assertNotNull(testContainerE);
        ContainerSchemaNode testSubContainerE = (ContainerSchemaNode) testContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-e"));
        assertNotNull(testSubContainerE);
        LeafSchemaNode testLeafE = (LeafSchemaNode) testSubContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-e"));
        assertNull(testLeafE);

        ContainerSchemaNode testContainerF = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-f"));
        assertNotNull(testContainerF);
        ContainerSchemaNode testSubContainerF = (ContainerSchemaNode) testContainerF.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-f"));
        assertNull(testSubContainerF);

        ContainerSchemaNode testContainerG = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-g"));
        assertNotNull(testContainerG);
        assertEquals(1, testContainerG.getAvailableAugmentations().size());
        LeafSchemaNode testLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-g"));
        assertNotNull(testLeafG);
        LeafSchemaNode augmentingTestLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-leaf-g"));
        assertNull(augmentingTestLeafG);
        AnyXmlSchemaNode augmentingTestAnyxmlG = (AnyXmlSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-anyxml-g"));
        assertNotNull(augmentingTestAnyxmlG);

        ContainerSchemaNode testContainerH = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-h"));
        assertNotNull(testContainerH);
        assertEquals(0, testContainerH.getChildNodes().size());
        assertEquals(0, testContainerH.getUses().size());

        ContainerSchemaNode testContainerI = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-i"));
        assertNotNull(testContainerI);
        assertEquals(1, testContainerI.getUses().size());
        ContainerSchemaNode testGroupingSubContainer = (ContainerSchemaNode) testContainerI.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        LeafSchemaNode testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNull(testGroupingLeaf);

        ContainerSchemaNode testContainerJ = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-j"));
        assertNotNull(testContainerJ);
        LeafSchemaNode testLeafJ = (LeafSchemaNode) testContainerJ.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-j"));
        assertNotNull(testLeafJ);

        ContainerSchemaNode testContainerK = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-k"));
        assertNotNull(testContainerK);
        assertEquals(1, testContainerK.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerK.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        assertEquals(1, testGroupingSubContainer.getAvailableAugmentations().size());
        LeafSchemaNode augmentingTestGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf"));
        assertNotNull(augmentingTestGroupingLeaf);
        LeafSchemaNode augmentingTestGroupingLeaf2 = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf-2"));
        assertNull(augmentingTestGroupingLeaf2);
    }

    @Test
    public void testAllFeaturesSupported() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(FOO_MODULE, BAR_MODULE);

        SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);

        Module testModule = schemaContext.findModuleByName("foo", null);
        assertNotNull(testModule);

        assertEquals(11, testModule.getChildNodes().size());

        ContainerSchemaNode testContainerA = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-a"));
        assertNotNull(testContainerA);
        LeafSchemaNode testLeafA = (LeafSchemaNode) testContainerA.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-a"));
        assertNotNull(testLeafA);


        ContainerSchemaNode testContainerB = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-b"));
        assertNotNull(testContainerB);
        LeafSchemaNode testLeafB = (LeafSchemaNode) testContainerB.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-b"));
        assertNotNull(testLeafB);

        ContainerSchemaNode testContainerC = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-c"));
        assertNotNull(testContainerC);
        LeafSchemaNode testLeafC = (LeafSchemaNode) testContainerC.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-c"));
        assertNotNull(testLeafC);

        ContainerSchemaNode testContainerD = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-d"));
        assertNotNull(testContainerD);
        LeafSchemaNode testLeafD = (LeafSchemaNode) testContainerD.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-d"));
        assertNotNull(testLeafD);

        ContainerSchemaNode testContainerE = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-e"));
        assertNotNull(testContainerE);
        ContainerSchemaNode testSubContainerE = (ContainerSchemaNode) testContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-e"));
        assertNotNull(testSubContainerE);
        LeafSchemaNode testLeafE = (LeafSchemaNode) testSubContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-e"));
        assertNotNull(testLeafE);

        ContainerSchemaNode testContainerF = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-f"));
        assertNotNull(testContainerF);
        ContainerSchemaNode testSubContainerF = (ContainerSchemaNode) testContainerF.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-f"));
        assertNotNull(testSubContainerF);
        ContainerSchemaNode testSubSubContainerF = (ContainerSchemaNode) testSubContainerF.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subsubcontainer-f"));
        assertNotNull(testSubSubContainerF);
        LeafSchemaNode testLeafF = (LeafSchemaNode) testSubSubContainerF.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-f"));
        assertNotNull(testLeafF);

        ContainerSchemaNode testContainerG = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-g"));
        assertNotNull(testContainerG);
        assertEquals(2, testContainerG.getAvailableAugmentations().size());
        LeafSchemaNode testLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-g"));
        assertNotNull(testLeafG);
        LeafSchemaNode augmentingTestLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-leaf-g"));
        assertNotNull(augmentingTestLeafG);
        AnyXmlSchemaNode augmentingTestAnyxmlG = (AnyXmlSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-anyxml-g"));
        assertNotNull(augmentingTestAnyxmlG);

        ContainerSchemaNode testContainerH = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-h"));
        assertNotNull(testContainerH);
        assertEquals(1, testContainerH.getUses().size());
        ContainerSchemaNode testGroupingSubContainer = (ContainerSchemaNode) testContainerH.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        LeafSchemaNode testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNotNull(testGroupingLeaf);

        ContainerSchemaNode testContainerI = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-i"));
        assertNotNull(testContainerI);
        assertEquals(1, testContainerI.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerI.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNotNull(testGroupingLeaf);

        ContainerSchemaNode testContainerJ = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-j"));
        assertNotNull(testContainerJ);
        LeafSchemaNode testLeafJ = (LeafSchemaNode) testContainerJ.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-j"));
        assertNotNull(testLeafJ);

        ContainerSchemaNode testContainerK = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-k"));
        assertNotNull(testContainerK);
        assertEquals(1, testContainerK.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerK.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        assertEquals(1, testGroupingSubContainer.getAvailableAugmentations().size());
        LeafSchemaNode augmentingTestGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf"));
        assertNotNull(augmentingTestGroupingLeaf);
        LeafSchemaNode augmentingTestGroupingLeaf2 = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf-2"));
        assertNotNull(augmentingTestGroupingLeaf2);
        testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNotNull(testGroupingLeaf);
    }

    @Test
    public void testNoFeaturesSupported() throws ReactorException {
        Predicate<QName> isFeatureSupported = qName -> false;

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild(isFeatureSupported);
        reactor.addSources(FOO_MODULE, BAR_MODULE);

        SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);

        Module testModule = schemaContext.findModuleByName("foo", null);
        assertNotNull(testModule);

        assertEquals(6, testModule.getChildNodes().size());

        ContainerSchemaNode testContainerE = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-e"));
        assertNotNull(testContainerE);
        ContainerSchemaNode testSubContainerE = (ContainerSchemaNode) testContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-e"));
        assertNotNull(testSubContainerE);
        LeafSchemaNode testLeafE = (LeafSchemaNode) testSubContainerE.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-e"));
        assertNull(testLeafE);

        ContainerSchemaNode testContainerF = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-f"));
        assertNotNull(testContainerF);
        ContainerSchemaNode testSubContainerF = (ContainerSchemaNode) testContainerF.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-subcontainer-f"));
        assertNull(testSubContainerF);

        ContainerSchemaNode testContainerG = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-g"));
        assertNotNull(testContainerG);
        assertEquals(1, testContainerG.getAvailableAugmentations().size());
        LeafSchemaNode testLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-leaf-g"));
        assertNotNull(testLeafG);
        LeafSchemaNode augmentingTestLeafG = (LeafSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-leaf-g"));
        assertNull(augmentingTestLeafG);
        AnyXmlSchemaNode augmentingTestAnyxmlG = (AnyXmlSchemaNode) testContainerG.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-anyxml-g"));
        assertNotNull(augmentingTestAnyxmlG);

        ContainerSchemaNode testContainerH = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-h"));
        assertNotNull(testContainerH);
        assertEquals(0, testContainerH.getChildNodes().size());
        assertEquals(0, testContainerH.getUses().size());

        ContainerSchemaNode testContainerI = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-i"));
        assertNotNull(testContainerI);
        assertEquals(1, testContainerI.getUses().size());
        ContainerSchemaNode testGroupingSubContainer = (ContainerSchemaNode) testContainerI.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        LeafSchemaNode testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNull(testGroupingLeaf);

        ContainerSchemaNode testContainerK = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container-k"));
        assertNotNull(testContainerK);
        assertEquals(1, testContainerK.getUses().size());
        testGroupingSubContainer = (ContainerSchemaNode) testContainerK.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-subcontainer"));
        assertNotNull(testGroupingSubContainer);
        assertEquals(1, testGroupingSubContainer.getAvailableAugmentations().size());
        LeafSchemaNode augmentingTestGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf"));
        assertNull(augmentingTestGroupingLeaf);
        LeafSchemaNode augmentingTestGroupingLeaf2 = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "augmenting-test-grouping-leaf-2"));
        assertNull(augmentingTestGroupingLeaf2);
        testGroupingLeaf = (LeafSchemaNode) testGroupingSubContainer.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-grouping-leaf"));
        assertNull(testGroupingLeaf);
    }
}
