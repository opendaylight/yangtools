/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

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
    public void testCheckedFieldCastIdentity() {
        assertNull(CodeHelpers.checkFieldCastIdentity(Identity.class, "foo", null));
        assertSame(Identity.class, CodeHelpers.checkFieldCastIdentity(Identity.class, "foo", Identity.class));
        assertSame(DerivedIdentity.class, CodeHelpers.checkFieldCastIdentity(Identity.class, "foo",
            DerivedIdentity.class));

        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkFieldCastIdentity(Identity.class, "foo", new Object()));
        assertThat(iae.getMessage(), allOf(
            startsWith("Invalid input value \"java.lang.Object"),
            endsWith("\" for property \"foo\"")));

        iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkFieldCastIdentity(Identity.class, "foo", BaseIdentity.class));
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
    public void testCheckListFieldCastIdentity() {
        assertNull(CodeHelpers.checkListFieldCastIdentity(Identity.class, "foo", null));
        assertSame(List.of(), CodeHelpers.checkListFieldCastIdentity(Identity.class, "foo", List.of()));

        final var list = List.of(Identity.class);
        assertSame(list, CodeHelpers.checkListFieldCastIdentity(Identity.class, "foo", list));
        final var derivedList = List.of(DerivedIdentity.class);
        assertSame(derivedList, CodeHelpers.checkListFieldCastIdentity(Identity.class, "foo", derivedList));

        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkListFieldCastIdentity(Identity.class, "foo", Collections.singletonList(null)));
        assertThat(iae.getCause(), instanceOf(NullPointerException.class));

        iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkListFieldCastIdentity(Identity.class, "foo", List.of(new Object())));
        assertThat(iae.getCause(), instanceOf(ClassCastException.class));

        iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkListFieldCastIdentity(Identity.class, "foo", List.of(BaseIdentity.class)));
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
    public void testCheckSetFieldCastIdentity() {
        assertNull(CodeHelpers.checkSetFieldCastIdentity(Identity.class, "foo", null));
        assertSame(Set.of(), CodeHelpers.checkSetFieldCastIdentity(Identity.class, "foo", Set.of()));

        final var set = Set.of(Identity.class);
        assertSame(set, CodeHelpers.checkSetFieldCastIdentity(Identity.class, "foo", set));
        final var derivedSet = Set.of(DerivedIdentity.class);
        assertSame(derivedSet, CodeHelpers.checkSetFieldCastIdentity(Identity.class, "foo", derivedSet));

        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkSetFieldCastIdentity(Identity.class, "foo", Collections.singleton(null)));
        assertThat(iae.getCause(), instanceOf(NullPointerException.class));

        iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkSetFieldCastIdentity(Identity.class, "foo", Set.of(new Object())));
        assertThat(iae.getCause(), instanceOf(ClassCastException.class));

        iae = assertThrows(IllegalArgumentException.class,
            () -> CodeHelpers.checkSetFieldCastIdentity(Identity.class, "foo", Set.of(BaseIdentity.class)));
        assertThat(iae.getCause(), instanceOf(ClassCastException.class));
    }

    private interface Identity extends BaseIdentity {

    }

    private interface DerivedIdentity extends Identity {

    }
}
