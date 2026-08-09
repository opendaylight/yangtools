/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.ScalarTypes;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.UnknownLeafrefType;

@SuppressWarnings("removal")
class SystemLeafsetTest {
    @Test
    void reportedRawTypeIsSet() {
        assertEquals(TypeName.ofClass(Set.class), SystemLeafset.UNKNOWN.getRawType().name());
    }

    @Test
    void unknownHasEmptyParameters() {
        assertEquals(List.of(), SystemLeafset.UNKNOWN.getActualTypeArguments());
    }

    @Test
    void unknownLeafrefIsSquashed() {
        assertSame(SystemLeafset.UNKNOWN, SystemLeafset.of(UnknownLeafrefType.INSTANCE));
    }

    @Test
    void invalidTypeIsRejected() {
        final var ref = TypeRef.of(TypeName.of("random", "ref"));
        final var ex = assertThrows(IllegalArgumentException.class, () -> SystemLeafset.of(ref));
        assertEquals("TypeRef{name=random.ref} is not an allowed type", ex.getMessage());
    }

    @Test
    void validTypeIsPropagated() {
        final var set = SystemLeafset.of(ScalarTypes.BINARY);
        assertEquals(List.of(ScalarTypes.BINARY), set.getActualTypeArguments());
    }
}
