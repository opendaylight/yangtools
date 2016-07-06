/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.opendaylight.yangtools.yang.binding.util.AugmentationFieldGetter.getGetter;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;

public class AugmentationFieldGetterTest {

    @Test
    public void getGetterTest() throws Exception {
        assertNotNull(getGetter(AugmentationHolder.class));
        assertTrue(getGetter(AugmentationHolder.class)
                .getAugmentations(mock(AugmentationHolder.class)).isEmpty());
        assertTrue(getGetter(Object.class).getAugmentations(null).isEmpty());
        assertTrue(getGetter(TestAugmentationWrongTypeClass.class).getAugmentations(null).isEmpty());

        final AugmentationFieldGetter augmentationFieldGetter = getGetter(TestAugmentationClass.class);
        final Augmentation augmentation = mock(Augmentation.class);
        final TestAugmentationClass testAugmentationClass = new TestAugmentationClass();

        testAugmentationClass.addAugmentation(augmentation, augmentation);
        assertNotNull(augmentationFieldGetter.getAugmentations(testAugmentationClass));
        assertEquals(1, augmentationFieldGetter.getAugmentations(testAugmentationClass).size());
    }

    @Test(expected = IllegalStateException.class)
    public void getWrongGetterTest() throws Exception {
        final AugmentationFieldGetter augmentationFieldGetter = getGetter(TestAugmentationClass.class);
        augmentationFieldGetter.getAugmentations(new String());
        fail("Expected IllegalStateException");
    }

    @Test
    public void getNoGetterTest() throws Exception {
        assertTrue(getGetter(Object.class).getAugmentations(null).isEmpty());
    }

    private final class TestAugmentationClass {
        private Map augmentation = new HashMap();

        void addAugmentation(Augmentation key, Augmentation value){
            augmentation.put(key, value);
        }
    }

    private final class TestAugmentationWrongTypeClass {
        private String augmentation;
    }
}