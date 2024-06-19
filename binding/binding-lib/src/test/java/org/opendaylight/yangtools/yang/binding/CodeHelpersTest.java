/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class CodeHelpersTest {
    @Test
    public void testCheckedFieldCast() {
        assertNull(CodeHelpers.checkFieldCast(CodeHelpersTest.class, "foo", null));
        assertSame(this, CodeHelpers.checkFieldCast(CodeHelpersTest.class, "foo", this));

        final IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkFieldCast(CodeHelpersTest.class, "foo", new Object()));
        assertThat(iae.getCause(), instanceOf(ClassCastException.class));
    }

    @Test
    public void testCheckListFieldCast() {
        assertNull(CodeHelpers.checkListFieldCast(CodeHelpersTest.class, "foo", null));
        assertSame(List.of(), CodeHelpers.checkListFieldCast(CodeHelpersTest.class, "foo", List.of()));
        final var list = List.of(this);
        assertSame(list, CodeHelpers.checkListFieldCast(CodeHelpersTest.class, "foo", list));

        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkListFieldCast(CodeHelpersTest.class, "foo", Collections.singletonList(null)));
        assertThat(iae.getCause(), instanceOf(NullPointerException.class));

        iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkListFieldCast(CodeHelpersTest.class, "foo", List.of(new Object())));
        assertThat(iae.getCause(), instanceOf(ClassCastException.class));
    }

    @Test
    public void testCheckSetFieldCast() {
        assertNull(CodeHelpers.checkSetFieldCast(CodeHelpersTest.class, "foo", null));
        assertSame(Set.of(), CodeHelpers.checkSetFieldCast(CodeHelpersTest.class, "foo", Set.of()));
        final var list = Set.of(this);
        assertSame(list, CodeHelpers.checkSetFieldCast(CodeHelpersTest.class, "foo", list));

        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkSetFieldCast(CodeHelpersTest.class, "foo", Collections.singleton(null)));
        assertThat(iae.getCause(), instanceOf(NullPointerException.class));

        iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkSetFieldCast(CodeHelpersTest.class, "foo", Set.of(new Object())));
        assertThat(iae.getCause(), instanceOf(ClassCastException.class));
    }

    @Test
    public void testCheckEnumName() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> CodeHelpers.checkEnum(null, "xyzzy"));
        assertEquals("\"xyzzy\" is not a valid name", ex.getMessage());

        final var obj = mock(EnumTypeObject.class);
        assertSame(obj, CodeHelpers.checkEnum(obj, "xyzzy"));
    }

    @Test
    public void testCheckEnumValue() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> CodeHelpers.checkEnum(null, 1234));
        assertEquals("1234 is not a valid value", ex.getMessage());

        final var obj = mock(EnumTypeObject.class);
        assertSame(obj, CodeHelpers.checkEnum(obj, 1234));
    }
}
