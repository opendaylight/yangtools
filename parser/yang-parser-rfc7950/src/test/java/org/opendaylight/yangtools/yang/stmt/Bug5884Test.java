/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

import java.util.Iterator;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

public class Bug5884Test extends AbstractYangTest {
    @Test
    public void testBug5884() {
        final var context = assertEffectiveModelDir("/bugs/bug5884");

        final var root = QName.create("urn:yang.foo", "2016-01-01", "main-container");
        final var choice = QName.create(root, "test-choice");
        final var testContainerQname = QName.create(root, "test");
        final var foo = context.findModule("foo", Revision.of("2016-01-01")).orElseThrow();
        final var rootContainer = (ContainerSchemaNode) context.getDataChildByName(root);
        final var testContainer = (ContainerSchemaNode) rootContainer.getDataChildByName(testContainerQname);
        final var dataChildByName = (ChoiceSchemaNode) testContainer.getDataChildByName(choice);

        testIterator(foo.getAugmentations().iterator());
        testIterator(dataChildByName.getAvailableAugmentations().iterator());
    }

    private static void testIterator(final Iterator<? extends AugmentationSchemaNode> iterator) {
        while (iterator.hasNext()) {
            var allAugments = iterator.next();
            final var currentChoice = allAugments.getChildNodes().iterator().next();
            assertThat(currentChoice, isA(CaseSchemaNode.class));
        }
    }
}