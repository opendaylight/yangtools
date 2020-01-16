/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.stmt.TestUtils;

public class YinFileAugmentStmtTest extends AbstractYinModulesTest {

    @Test
    public void testAugment() {
        final Module testModule = TestUtils.findModule(context, "main-impl").get();
        assertNotNull(testModule);

        final Collection<? extends AugmentationSchemaNode> augmentations = testModule.getAugmentations();
        assertEquals(1, augmentations.size());

        final Iterator<? extends AugmentationSchemaNode> augmentIterator = augmentations.iterator();
        final AugmentationSchemaNode augment = augmentIterator.next();
        assertNotNull(augment);
        assertTrue(augment.getTargetPath().toString().contains(
                "(urn:opendaylight:params:xml:ns:yang:controller:config?revision=2013-04-05)modules, "
                        + "(urn:opendaylight:params:xml:ns:yang:controller:config?revision=2013-04-05)module, "
                        + "(urn:opendaylight:params:xml:ns:yang:controller:config?revision=2013-04-05)configuration"));

        assertEquals(1, augment.getChildNodes().size());
        final DataSchemaNode caseNode = augment.findDataChildByName(
            QName.create(testModule.getQNameModule(), "main-impl")).get();
        assertThat(caseNode, isA(CaseSchemaNode.class));
    }
}
