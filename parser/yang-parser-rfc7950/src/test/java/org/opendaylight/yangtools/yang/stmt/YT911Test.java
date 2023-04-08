/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

public class YT911Test extends AbstractYangTest {
    private static final QName FOO = QName.create("foo", "2018-10-22", "foo");
    private static final QName BAR = QName.create(FOO, "bar");

    @Test
    public void testAugmentationConfig() {
        final var context = assertEffectiveModel("/bugs/YT911/foo.yang");
        final var foo = context.getDataChildByName(FOO);
        assertEquals(Optional.of(Boolean.FALSE), foo.effectiveConfig());
        assertTrue(foo instanceof ContainerSchemaNode);

        // Instantiated node
        final var bar = ((ContainerSchemaNode) foo).findDataTreeChild(BAR).orElseThrow();
        assertEquals(Optional.of(Boolean.FALSE), bar.effectiveConfig());
        assertTrue(foo instanceof ContainerSchemaNode);

        // Original augmentation node
        final var aug = ((ContainerSchemaNode) foo).getAvailableAugmentations().iterator().next();
        final var augBar = aug.findDataTreeChild(BAR).orElseThrow();
        assertEquals(Optional.empty(), augBar.effectiveConfig());
        assertTrue(foo instanceof ContainerSchemaNode);
    }
}
