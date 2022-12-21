/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

class YT911Test extends AbstractYangTest {
    private static final QName FOO = QName.create("foo", "2018-10-22", "foo");
    private static final QName BAR = QName.create(FOO, "bar");

    @Test
    void testAugmentationConfig() {
        final var context = assertEffectiveModel("/bugs/YT911/foo.yang");
        final DataSchemaNode foo = context.findDataChildByName(FOO).get();
        assertEquals(Optional.of(Boolean.FALSE), foo.effectiveConfig());
        assertTrue(foo instanceof ContainerSchemaNode);

        // Instantiated node
        final DataSchemaNode bar = ((ContainerSchemaNode) foo).findDataTreeChild(BAR).get();
        assertEquals(Optional.of(Boolean.FALSE), bar.effectiveConfig());
        assertTrue(foo instanceof ContainerSchemaNode);

        // Original augmentation node
        final AugmentationSchemaNode aug = ((ContainerSchemaNode) foo).getAvailableAugmentations().iterator().next();
        final DataSchemaNode augBar = aug.findDataTreeChild(BAR).get();
        assertEquals(Optional.empty(), augBar.effectiveConfig());
        assertTrue(foo instanceof ContainerSchemaNode);
    }
}
