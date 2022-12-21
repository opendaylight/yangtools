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

import java.util.Collection;
import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

class YinFileAugmentStmtTest extends AbstractYinModulesTest {
    @Test
    void testAugment() {
        final Module testModule = context.findModules("main-impl").iterator().next();
        assertNotNull(testModule);

        final Collection<? extends AugmentationSchemaNode> augmentations = testModule.getAugmentations();
        assertEquals(1, augmentations.size());

        final Iterator<? extends AugmentationSchemaNode> augmentIterator = augmentations.iterator();
        final AugmentationSchemaNode augment = augmentIterator.next();
        assertNotNull(augment);
        assertThat(augment.getTargetPath().toString(), containsString(
            "(urn:opendaylight:params:xml:ns:yang:controller:config?revision=2013-04-05)modules, module, "
                + "configuration"));

        assertEquals(1, augment.getChildNodes().size());
        final DataSchemaNode caseNode = augment.findDataChildByName(
            QName.create(testModule.getQNameModule(), "main-impl")).get();
        assertInstanceOf(CaseSchemaNode.class, caseNode);
    }
}
