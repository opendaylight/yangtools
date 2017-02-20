/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug5884Test {
    private static final String NS = "urn:yang.foo";
    private static final String REV = "2016-01-01";

    @Test
    public void testBug5884() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5884");
        assertNotNull(context);

        final QName root = QName.create(NS, REV, "main-container");
        final QName choice = QName.create(NS, REV, "test-choice");
        final QName testContainerQname = QName.create(NS, REV, "test");
        final Date date = SimpleDateFormatUtil.getRevisionFormat().parse("2016-01-01");
        final Module foo = context.findModuleByName("foo", date);
        final ContainerSchemaNode rootContainer = (ContainerSchemaNode) context.getDataChildByName(root);
        final ContainerSchemaNode testContainer = (ContainerSchemaNode) rootContainer.getDataChildByName(
            testContainerQname);
        final ChoiceSchemaNode dataChildByName = (ChoiceSchemaNode) testContainer.getDataChildByName(choice);
        final Set<AugmentationSchema> augmentations = foo.getAugmentations();
        final Set<AugmentationSchema> availableAugmentations = dataChildByName.getAvailableAugmentations();
        final Iterator<AugmentationSchema> iterator = augmentations.iterator();
        final Iterator<AugmentationSchema> availableIterator = availableAugmentations.iterator();

        testIterator(iterator);
        testIterator(availableIterator);
    }

    private static void testIterator(final Iterator<AugmentationSchema> iterator) {
        while (iterator.hasNext()) {
            AugmentationSchema allAugments = iterator.next();
            final DataSchemaNode currentChoice = allAugments.getChildNodes().iterator().next();
            assertTrue(currentChoice instanceof ChoiceCaseNode);
        }
    }
}