/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

class AugmentToExtensionTest extends AbstractYangTest {
    @Test
    void testIncorrectPath() throws Exception {
        assertInferenceExceptionDir("/augment-to-extension-test/incorrect-path",
            startsWith("Augment target "
                + "'Descendant{qnames=[(uri:augment-module?revision=2014-10-07)my-extension-name-a, input]}'"
                + " not found [at "));
    }

    /*
     * FIXME: Figure way to determine use case of tail-f:input without hacks
     */
    @Test
    void testCorrectPathIntoUnsupportedTarget() throws Exception {
        final Module devicesModule =
            assertEffectiveModelDir("/augment-to-extension-test/correct-path-into-unsupported-target")
                .findModules("augment-module").iterator().next();
        final ContainerSchemaNode devicesContainer = (ContainerSchemaNode) devicesModule.getDataChildByName(
            QName.create(devicesModule.getQNameModule(), "my-container"));
        for (final UsesNode usesNode : devicesContainer.getUses()) {
            assertTrue(usesNode.getAugmentations().isEmpty());
        }
    }

    @Test
    void testCorrectAugment() throws Exception {
        final Module devicesModule = assertEffectiveModelDir("/augment-to-extension-test/correct-augment")
            .findModules("augment-module").iterator().next();

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
