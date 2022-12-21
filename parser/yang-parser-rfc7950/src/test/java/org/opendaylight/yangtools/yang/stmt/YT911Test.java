/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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
        final var foo = assertInstanceOf(ContainerSchemaNode.class, context.dataChildByName(FOO));
        assertEquals(Optional.of(Boolean.FALSE), foo.effectiveConfig());

        // Instantiated node
        final DataSchemaNode bar = foo.findDataTreeChild(BAR).orElseThrow();
        assertEquals(Optional.of(Boolean.FALSE), bar.effectiveConfig());

        // Original augmentation node
        final AugmentationSchemaNode aug = foo.getAvailableAugmentations().iterator().next();
        final DataSchemaNode augBar = aug.findDataTreeChild(BAR).orElseThrow();
        assertEquals(Optional.empty(), augBar.effectiveConfig());
    }
}
