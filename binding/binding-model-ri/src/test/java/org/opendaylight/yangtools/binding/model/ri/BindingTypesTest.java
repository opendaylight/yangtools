/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;

class BindingTypesTest {
    @Test
    void staticBindingTypesTest() {
        assertEquals(Types.typeForClass(Augmentable.class), BindingTypes.AUGMENTABLE);
        assertEquals(Types.typeForClass(Augmentation.class), BindingTypes.AUGMENTATION);
        assertEquals(Types.typeForClass(BaseIdentity.class), BindingTypes.BASE_IDENTITY);
        assertEquals(Types.typeForClass(DataObject.class), BindingTypes.DATA_OBJECT);
        assertEquals(Types.typeForClass(EntryObject.class), BindingTypes.ENTRY_OBJECT);
        assertEquals(Types.typeForClass(Key.class), BindingTypes.KEY);
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
        assertEquals("Augmentable", augmentableType.getName());
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
        assertEquals("Augmentation", augmentationType.getName());
    }

    @Test
    void testNotificationNull() {
        assertThrows(NullPointerException.class, () -> BindingTypes.notification(null));
    }

    @Test
    void testNotification() {
        final var notificationType = BindingTypes.notification(Types.objectType());
        assertEquals(Types.typeForClass(Notification.class), notificationType.getRawType());
        assertArrayEquals(new Object[] { Types.objectType() }, notificationType.getActualTypeArguments());
    }

    @Test
    void testDataRoot() {
        final var type = assertInstanceOf(ParameterizedType.class, BindingTypes.dataRoot(Types.objectType()));
        assertEquals(Types.typeForClass(DataRoot.class), type.getRawType());
        assertArrayEquals(new Type[] { Types.typeForClass(Object.class) }, type.getActualTypeArguments());
    }
}
