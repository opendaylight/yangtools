/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.EnumTypeObject;

@ExtendWith(MockitoExtension.class)
class CodeHelpersTest {
    @Mock
    private EnumTypeObject enumTypeObject;

    @Test
    void testCheckedFieldCast() {
        assertNull(CodeHelpers.checkFieldCast(CodeHelpersTest.class, "foo", null));
        assertSame(this, CodeHelpers.checkFieldCast(CodeHelpersTest.class, "foo", this));

        final var iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkFieldCast(CodeHelpersTest.class, "foo", new Object()));
        assertInstanceOf(ClassCastException.class, iae.getCause());
    }

    @Test
    void testCheckListFieldCast() {
        assertNull(CodeHelpers.checkListFieldCast(CodeHelpersTest.class, "foo", null));
        assertSame(List.of(), CodeHelpers.checkListFieldCast(CodeHelpersTest.class, "foo", List.of()));
        final var list = List.of(this);
        assertSame(list, CodeHelpers.checkListFieldCast(CodeHelpersTest.class, "foo", list));

        var iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkListFieldCast(CodeHelpersTest.class, "foo", Collections.singletonList(null)));
        assertInstanceOf(NullPointerException.class, iae.getCause());

        iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkListFieldCast(CodeHelpersTest.class, "foo", List.of(new Object())));
        assertInstanceOf(ClassCastException.class, iae.getCause());
    }

    @Test
    void testCheckSetFieldCast() {
        assertNull(CodeHelpers.checkSetFieldCast(CodeHelpersTest.class, "foo", null));
        assertSame(Set.of(), CodeHelpers.checkSetFieldCast(CodeHelpersTest.class, "foo", Set.of()));
        final var list = Set.of(this);
        assertSame(list, CodeHelpers.checkSetFieldCast(CodeHelpersTest.class, "foo", list));

        var iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkSetFieldCast(CodeHelpersTest.class, "foo", Collections.singleton(null)));
        assertInstanceOf(NullPointerException.class, iae.getCause());

        iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkSetFieldCast(CodeHelpersTest.class, "foo", Set.of(new Object())));
        assertInstanceOf(ClassCastException.class, iae.getCause());
    }

    @Test
    void testCheckEnumName() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> CodeHelpers.checkEnum(null, "xyzzy"));
        assertEquals("\"xyzzy\" is not a valid name", ex.getMessage());

        assertSame(enumTypeObject, CodeHelpers.checkEnum(enumTypeObject, "xyzzy"));
    }

    @Test
    void testCheckEnumValue() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> CodeHelpers.checkEnum(null, 1234));
        assertEquals("1234 is not a valid value", ex.getMessage());

        assertSame(enumTypeObject, CodeHelpers.checkEnum(enumTypeObject, 1234));
    }
}
