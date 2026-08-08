/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TypeNameTest {
    @Test
    void testOperations() {
        final var byteName = TypeName.ofClass(byte.class);
        assertEquals("", byteName.packageName());
        assertEquals("byte", byteName.simpleName());
        assertEquals("byte", byteName.toString());
        assertNull(byteName.immediatelyEnclosingClass());
        assertSame(byteName, byteName.topLevelClass());
        assertEquals(List.of("byte"), byteName.localNameComponents());
        assertEquals("byte", byteName.localName());

        final var charName = byteName.createSibling("char");
        assertEquals("", charName.packageName());
        assertEquals("char", charName.simpleName());
        assertEquals("char", charName.toString());
        assertNull(charName.immediatelyEnclosingClass());
        assertSame(charName, charName.topLevelClass());
        assertEquals(List.of("char"), charName.localNameComponents());
        assertEquals("char", charName.localName());

        final var threadName = TypeName.ofClass(Thread.class);
        assertEquals("java.lang", threadName.packageName());
        assertEquals("Thread", threadName.simpleName());
        assertEquals("java.lang.Thread", threadName.toString());
        assertNull(threadName.immediatelyEnclosingClass());
        assertTrue(threadName.canCreateEnclosed("Foo"));
        assertFalse(threadName.canCreateEnclosed("Thread"));
        assertEquals(threadName, TypeName.of("java.lang", "Thread"));
        assertSame(threadName, threadName.topLevelClass());
        assertEquals(List.of("Thread"), threadName.localNameComponents());
        assertEquals("Thread", threadName.localName());

        final var stringName = threadName.createSibling("String");
        assertEquals("java.lang", stringName.packageName());
        assertEquals("String", stringName.simpleName());
        assertEquals("java.lang.String", stringName.toString());
        assertNull(stringName.immediatelyEnclosingClass());
        assertEquals(stringName, TypeName.of("java.lang", "String"));

        final var enclosedName = threadName.createEnclosed("Foo");
        assertEquals("java.lang", enclosedName.packageName());
        assertEquals("Foo", enclosedName.simpleName());
        assertEquals("java.lang.Thread.Foo", enclosedName.toString());
        assertEquals(threadName, enclosedName.immediatelyEnclosingClass());
        assertSame(threadName, enclosedName.topLevelClass());
        assertEquals(List.of("Thread", "Foo"), enclosedName.localNameComponents());
        assertEquals("Thread.Foo", enclosedName.localName());

        final var uehName = TypeName.ofClass(Thread.UncaughtExceptionHandler.class);
        assertEquals("java.lang", uehName.packageName());
        assertEquals("UncaughtExceptionHandler", uehName.simpleName());
        assertEquals("java.lang.Thread.UncaughtExceptionHandler", uehName.toString());
        assertEquals(threadName, uehName.immediatelyEnclosingClass());
        assertTrue(uehName.canCreateEnclosed("Foo"));
        assertFalse(uehName.canCreateEnclosed("Thread"));
        assertFalse(uehName.canCreateEnclosed("UncaughtExceptionHandler"));

        final var siblingName = uehName.createSibling("Foo");
        assertEquals("java.lang", siblingName.packageName());
        assertEquals("Foo", siblingName.simpleName());
        assertEquals("java.lang.Thread.Foo", siblingName.toString());
        assertEquals(threadName, siblingName.immediatelyEnclosingClass());
        assertTrue(siblingName.canCreateEnclosed("UncaughtExceptionHandler"));
        assertFalse(siblingName.canCreateEnclosed("Thread"));
        assertFalse(siblingName.canCreateEnclosed("Foo"));

        assertTrue(threadName.equals(TypeName.ofClass(Thread.class)));
        assertTrue(threadName.equals(threadName));
        assertFalse(threadName.equals(null));
        assertFalse(threadName.equals("foo"));
    }

    @Test
    void testCreateEmptyPackage() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> TypeName.of("", "Foo"));
        assertEquals("empty package name", ex.getMessage());
    }

    @Test
    void testCreateEmptyName() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> TypeName.of("foo", ""));
        assertEquals("empty simple name", ex.getMessage());
    }

    @Test
    void testCanCreateEnclosedPrimitive() {
        final var ex = assertThrows(UnsupportedOperationException.class,
            () -> TypeName.ofClass(byte.class).canCreateEnclosed("foo"));
        assertEquals("Primitive type byte cannot enclose type foo", ex.getMessage());
    }

    @Test
    void testCreateEnclosedPrimitive() {
        final var ex = assertThrows(UnsupportedOperationException.class,
            () -> TypeName.ofClass(byte.class).createEnclosed("foo"));
        assertEquals("Primitive type byte cannot enclose type foo", ex.getMessage());
    }

    @Test
    void testHashCode() {
        var baseType1 = TypeName.of("org.opendaylight.yangtools.test", "Test");
        var baseType2 = TypeName.of("org.opendaylight.yangtools.test", "Test2");
        assertNotEquals(baseType1.hashCode(), baseType2.hashCode());
    }

    @Test
    void testToString() {
        var baseType = TypeName.of("org.opendaylight.yangtools.test", "Test");
        assertTrue(baseType.toString().contains("org.opendaylight.yangtools.test.Test"));
        baseType = TypeName.ofClass(byte[].class);
        assertTrue(baseType.toString().contains("byte[]"));
    }

    @Test
    void testEquals() {
        final var baseType1 = TypeName.of("org.opendaylight.yangtools.test", "Test");
        final var baseType2 = TypeName.of("org.opendaylight.yangtools.test", "Test2");
        final var baseType4 = TypeName.of("org.opendaylight.yangtools.test", "Test");
        final var baseType5 = TypeName.of("org.opendaylight.yangtools.test1", "Test");

        assertFalse(baseType1.equals(baseType2));
        assertFalse(baseType1.equals(null));
        assertTrue(baseType1.equals(baseType4));
        assertFalse(baseType1.equals(baseType5));
        assertFalse(baseType1.equals(null));
    }
}
