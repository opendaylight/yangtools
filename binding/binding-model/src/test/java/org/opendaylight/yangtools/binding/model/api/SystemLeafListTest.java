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
class SystemLeafListTest {
    @Test
    void reportedRawTypeIsSet() {
        assertEquals(TypeName.ofClass(Set.class), SystemLeafList.UNKNOWN.getRawType().name());
    }

    @Test
    void unknownHasEmptyParameters() {
        assertEquals(List.of(), SystemLeafList.UNKNOWN.getActualTypeArguments());
    }

    @Test
    void unknownLeafrefIsSquashed() {
        assertSame(SystemLeafList.UNKNOWN, SystemLeafList.of(UnknownLeafrefType.INSTANCE));
    }

    @Test
    void invalidTypeIsRejected() {
        final var ref = TypeRef.of(TypeName.of("random", "ref"));
        final var ex = assertThrows(IllegalArgumentException.class, () -> SystemLeafList.of(ref));
        assertEquals("TypeRef{name=random.ref} is not an allowed type", ex.getMessage());
    }

    @Test
    void validTypeIsPropagated() {
        final var set = SystemLeafList.of(ScalarTypes.BINARY);
        assertEquals(List.of(ScalarTypes.BINARY), set.getActualTypeArguments());
    }
}
