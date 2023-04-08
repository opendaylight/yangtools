/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;

class YinFileAugmentStmtTest extends AbstractYinModulesTest {
    @Test
    void testAugment() {
        final var testModule = context.findModules("main-impl").iterator().next();
        assertNotNull(testModule);

        final var augmentations = testModule.getAugmentations();
        assertEquals(1, augmentations.size());

        final var augment = augmentations.iterator().next();
        assertThat(augment.getTargetPath().toString(), containsString(
            "(urn:opendaylight:params:xml:ns:yang:controller:config?revision=2013-04-05)modules, module, "
                + "configuration"));

        assertEquals(1, augment.getChildNodes().size());
        assertInstanceOf(CaseSchemaNode.class, augment.getDataChildByName(
            QName.create(testModule.getQNameModule(), "main-impl")));
    }
}
