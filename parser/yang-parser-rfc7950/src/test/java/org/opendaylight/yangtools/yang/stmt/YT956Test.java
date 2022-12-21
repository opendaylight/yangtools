/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

class YT956Test extends AbstractYangTest {
    private static final QName ANOTHER_CONTAINER = QName.create("http://www.example.com/anothermodule",
        "another-container");
    private static final QName FIRST_AUGMENT = QName.create("http://www.example.com/mainmodule", "first-augment");

    @Test
    void testAugmentationConditional() {
        final DataSchemaNode another = assertEffectiveModelDir("/bugs/YT956/")
            .getDataChildByName(ANOTHER_CONTAINER);
        assertThat(another, isA(ContainerSchemaNode.class));
        final ContainerSchemaNode anotherContainer = (ContainerSchemaNode) another;

        final DataSchemaNode first = anotherContainer.findDataChildByName(FIRST_AUGMENT).get();
        assertThat(first, isA(ContainerSchemaNode.class));
        final ContainerSchemaNode firstAugment = (ContainerSchemaNode) first;

        // Augmentation needs to be added
        assertEquals(3, firstAugment.getChildNodes().size());
    }
}
