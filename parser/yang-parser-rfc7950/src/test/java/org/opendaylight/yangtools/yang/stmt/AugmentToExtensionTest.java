/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

public class AugmentToExtensionTest {
    private SchemaContext context;

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void testIncorrectPath() throws Exception {
        context = TestUtils.loadModules(getClass().getResource("/augment-to-extension-test/incorrect-path").toURI());
    }

    /*
     * FIXME: Figure way to determine use case of tail-f:input without hacks
     */
    @Test
    public void testCorrectPathIntoUnsupportedTarget() throws Exception {

        context = TestUtils.loadModules(getClass().getResource(
                "/augment-to-extension-test/correct-path-into-unsupported-target").toURI());

        final Module devicesModule = TestUtils.findModule(context, "augment-module").get();
        final ContainerSchemaNode devicesContainer = (ContainerSchemaNode) devicesModule.getDataChildByName(
            QName.create(devicesModule.getQNameModule(), "my-container"));
        for (final UsesNode usesNode : devicesContainer.getUses()) {
            assertTrue(usesNode.getAugmentations().isEmpty());
        }
    }


    @Test
    public void testCorrectAugment() throws Exception {
        context = TestUtils.loadModules(getClass().getResource("/augment-to-extension-test/correct-augment").toURI());

        final Module devicesModule = TestUtils.findModule(context, "augment-module").get();

        final ContainerSchemaNode devicesContainer = (ContainerSchemaNode) devicesModule.getDataChildByName(QName
                .create(devicesModule.getQNameModule(), "my-container"));
        boolean augmentationIsInContainer = false;
        for (final UsesNode usesNode : devicesContainer.getUses()) {
            for (final AugmentationSchemaNode augmentationSchema : usesNode.getAugmentations()) {
                augmentationIsInContainer = true;
            }
        }

        assertTrue(augmentationIsInContainer);
    }

}
