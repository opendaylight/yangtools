/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 */
package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertTrue;

import org.opendaylight.yangtools.yang.stmt.test.StmtTestUtils;

import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class AugmentToExtensionTest {
    private Set<Module> modules;

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void testIncorrectPath() throws URISyntaxException, SourceException, ReactorException {
        modules = TestUtils.loadModules(getClass().getResource("/augment-to-extension-test/incorrect-path").toURI());

    }

    @Test
    public void testCorrectPathIntoUnsupportedTarget() throws URISyntaxException, SourceException, ReactorException {

        try {
        modules = TestUtils.loadModules(getClass().getResource(
                "/augment-to-extension-test/correct-path-into-unsupported-target").toURI());
        } catch (Exception e) {
            StmtTestUtils.log(e, "    ");
            throw e;
        }

        Module devicesModule = TestUtils.findModule(modules, "augment-module");

        ContainerSchemaNode devicesContainer = (ContainerSchemaNode) devicesModule.getDataChildByName("my-container");
        Set<UsesNode> uses = devicesContainer.getUses();

        for (UsesNode usesNode : uses) {
            assertTrue(usesNode.getAugmentations().isEmpty());
        }
    }

    @Test
    public void testCorrectAugment() throws URISyntaxException, SourceException, ReactorException {
        modules = TestUtils.loadModules(getClass().getResource("/augment-to-extension-test/correct-augment").toURI());

        Module devicesModule = TestUtils.findModule(modules, "augment-module");

        ContainerSchemaNode devicesContainer = (ContainerSchemaNode) devicesModule.getDataChildByName("my-container");
        Set<UsesNode> uses = devicesContainer.getUses();

        boolean augmentationIsInContainer = false;
        for (UsesNode usesNode : uses) {
            Set<AugmentationSchema> augmentations = usesNode.getAugmentations();
            for (AugmentationSchema augmentationSchema : augmentations) {
                augmentationIsInContainer = true;
            }
        }

        assertTrue(augmentationIsInContainer);
    }

}
