/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

class Bug5884Test extends AbstractYangTest {
    private static final String NS = "urn:yang.foo";
    private static final String REV = "2016-01-01";

    @Test
    void testBug5884() {
        final var context = assertEffectiveModelDir("/bugs/bug5884");

        final QName root = QName.create(NS, REV, "main-container");
        final QName choice = QName.create(NS, REV, "test-choice");
        final QName testContainerQname = QName.create(NS, REV, "test");
        final Module foo = context.findModule("foo", Revision.of("2016-01-01")).get();
        final ContainerSchemaNode rootContainer = (ContainerSchemaNode) context.getDataChildByName(root);
        final ContainerSchemaNode testContainer = (ContainerSchemaNode) rootContainer.getDataChildByName(
            testContainerQname);
        final ChoiceSchemaNode dataChildByName = (ChoiceSchemaNode) testContainer.getDataChildByName(choice);

        testIterator(foo.getAugmentations().iterator());
        testIterator(dataChildByName.getAvailableAugmentations().iterator());
    }

    private static void testIterator(final Iterator<? extends AugmentationSchemaNode> iterator) {
        while (iterator.hasNext()) {
            assertInstanceOf(CaseSchemaNode.class, iterator.next().getChildNodes().iterator().next());
        }
    }
}