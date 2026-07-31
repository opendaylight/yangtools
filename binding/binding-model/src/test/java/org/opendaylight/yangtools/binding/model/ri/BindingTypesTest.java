/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;

class BindingTypesTest {
    @Test
    void staticBindingTypesTest() {
        assertEquals(ConcreteType.ofClass(Augmentable.class), BindingTypes.AUGMENTABLE);
        assertEquals(ConcreteType.ofClass(Augmentation.class), BindingTypes.AUGMENTATION);
        assertEquals(ConcreteType.ofClass(EntryObject.class), BindingTypes.ENTRY_OBJECT);
    }

    @Test
    void testAugmentableNull() {
        assertThrows(NullPointerException.class, () -> BindingTypes.augmentable(null));
    }

    @Test
    void testChildOfNull() {
        assertThrows(NullPointerException.class, () -> BindingTypes.childOf(null));
    }

    @Test
    void testAugmentable() {
        final var augmentableType = BindingTypes.augmentable(Types.objectType());
        assertEquals("Augmentable", augmentableType.simpleName());
    }

    @Test
    void testChildOf() {
        assertNotNull(BindingTypes.childOf(Types.objectType()));
    }

    @Test
    void testAugmentationNull() {
        assertThrows(NullPointerException.class, () -> BindingTypes.augmentation(null));
    }

    @Test
    void testAugmentation() {
        final var augmentationType = BindingTypes.augmentation(Types.objectType());
        assertEquals("Augmentation", augmentationType.simpleName());
    }
}
