/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;

class FeatureSetTest {
    private static final @NonNull QName FOO_FOO = QName.create("foo", "foo");
    private static final @NonNull QName FOO_BAR = QName.create("foo", "bar");
    private static final @NonNull QName BAR_FOO = QName.create("bar", "foo");
    private static final @NonNull QName BAR_BAR = QName.create("bar", "bar");

    @Test
    void emptyIsSingleton() {
        assertSame(FeatureSet.of(), FeatureSet.of());
    }

    @Test
    void explicitContains() {
        final var set = FeatureSet.of(FOO_FOO, BAR_FOO);
        assertTrue(set.contains(FOO_FOO));
        assertFalse(set.contains(FOO_BAR));
        assertTrue(set.contains(BAR_FOO));
        assertFalse(set.contains(BAR_BAR));
    }

    @Test
    void explicitHashCodeEquals() {
        final var set = FeatureSet.of(FOO_FOO, BAR_FOO);
        final var other = FeatureSet.of(Set.of(FOO_FOO, BAR_FOO));
        assertEquals(set.hashCode(), other.hashCode());
        assertEquals(set, other);
    }

    @Test
    void sparseContains() {
        final var set = FeatureSet.builder()
            .addModuleFeatures(FOO_FOO.getModule(), Set.of(FOO_FOO.getLocalName()))
            .build();

        assertTrue(set.contains(FOO_FOO));
        assertFalse(set.contains(FOO_BAR));
        assertTrue(set.contains(BAR_FOO));
        assertTrue(set.contains(BAR_BAR));
    }

    @Test
    void sparseHashCodeEquals() {
        final var set = FeatureSet.builder()
            .addModuleFeatures(FOO_FOO.getModule(), Set.of(FOO_FOO.getLocalName()))
            .build();
        final var other = FeatureSet.builder()
            .addModuleFeatures(FOO_FOO.getModule(), Set.of(FOO_FOO.getLocalName()))
            .build();
        assertEquals(set.hashCode(), other.hashCode());
        assertEquals(set, other);
    }
}
