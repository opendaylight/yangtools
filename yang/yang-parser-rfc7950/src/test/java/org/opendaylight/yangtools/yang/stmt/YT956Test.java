/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public class YT956Test {
    private static final QName ANOTHER_CONTAINER = QName.create("http://www.example.com/anothermodule",
        "another-container");
    private static final QName FIRST_AUGMENT = QName.create("http://www.example.com/mainmodule", "first-augment");

    @Test
    public void testAugmentationConditional() throws Exception {
        final DataSchemaNode another = StmtTestUtils.parseYangSources("/bugs/YT956/")
                .findDataChildByName(ANOTHER_CONTAINER).get();
        assertThat(another, instanceOf(ContainerSchemaNode.class));
        final ContainerSchemaNode anotherContainer = (ContainerSchemaNode) another;

        final DataSchemaNode first = anotherContainer.findDataChildByName(FIRST_AUGMENT).get();
        assertThat(first, instanceOf(ContainerSchemaNode.class));
        final ContainerSchemaNode firstAugment = (ContainerSchemaNode) first;

        // Augmentation needs to be added
        assertEquals(1, firstAugment.getChildNodes().size());
    }
}
