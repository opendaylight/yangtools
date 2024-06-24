/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.opendaylight.yangtools.binding.model.ri.Types.typeForClass;

import org.junit.Test;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BindingTypesTest {
    @Test
    public void staticBindingTypesTest() {
        assertEquals("AUGMENTABLE", typeForClass(Augmentable.class), BindingTypes.AUGMENTABLE);
        assertEquals("AUGMENTATION", typeForClass(Augmentation.class), BindingTypes.AUGMENTATION);
        assertEquals("BASE_IDENTITY", typeForClass(BaseIdentity.class), BindingTypes.BASE_IDENTITY);
        assertEquals("DATA_OBJECT", typeForClass(DataObject.class), BindingTypes.DATA_OBJECT);
        assertEquals("DATA_ROOT", typeForClass(DataRoot.class), BindingTypes.DATA_ROOT);
        assertEquals("KEY_AWARE", typeForClass(KeyAware.class), BindingTypes.KEY_AWARE);
        assertEquals("KEY", typeForClass(Key.class), BindingTypes.KEY);
        assertEquals("INSTANCE_IDENTIFIER", typeForClass(InstanceIdentifier.class), BindingTypes.INSTANCE_IDENTIFIER);
    }

    @Test
    public void testAugmentableNull() {
        assertThrows(NullPointerException.class, () -> BindingTypes.augmentable(null));
    }

    @Test
    public void testChildOfNull() {
        assertThrows(NullPointerException.class, () -> BindingTypes.childOf(null));
    }

    @Test
    public void testAugmentable() {
        ParameterizedType augmentableType = BindingTypes.augmentable(Types.objectType());
        assertEquals("Augmentable", augmentableType.getName());
    }

    @Test
    public void testChildOf() {
        assertNotNull(BindingTypes.childOf(Types.objectType()));
    }

    @Test
    public void testAugmentationNull() {
        assertThrows(NullPointerException.class, () -> BindingTypes.augmentation(null));
    }

    @Test
    public void testAugmentation() {
        final ParameterizedType augmentationType = BindingTypes.augmentation(Types.objectType());
        assertEquals("Augmentation", augmentationType.getName());
    }

    @Test
    public void testNotificationNull() {
        assertThrows(NullPointerException.class, () -> BindingTypes.notification(null));
    }

    @Test
    public void testNotification() {
        final ParameterizedType notificationType = BindingTypes.notification(Types.objectType());
        assertEquals(Types.typeForClass(Notification.class), notificationType.getRawType());
        assertArrayEquals(new Object[] { Types.objectType() }, notificationType.getActualTypeArguments());
    }
}
