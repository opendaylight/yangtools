/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class CheckedValueTest {
    @Test
    void testNullValue() {
        assertThrows(NullPointerException.class, () -> CheckedValue.ofValue(null));
    }

    @Test
    void testExceptionGet() {
        var value = CheckedValue.ofException(new Exception());
        assertThrows(IllegalStateException.class, () -> value.get());
    }

    @Test
    void testValueException() {
        var value = CheckedValue.ofValue("foo");
        assertThrows(IllegalStateException.class, () -> value.getException());
    }

    @Test
    void testGet() {
        var value = "foo";
        var val = CheckedValue.ofValue(value);
        assertTrue(val.isPresent());
        assertSame(value, val.get());
    }

    @Test
    void testThrowableGetThrowable() {
        var cause = new Exception();
        var val = CheckedValue.ofException(cause);
        assertFalse(val.isPresent());
        assertSame(cause, val.getException());
    }

    @Test
    void testToString() {
        assertEquals("CheckedValue{first=foo}", CheckedValue.ofValue("foo").toString());
        assertEquals("CheckedValue{second=java.lang.NullPointerException: foo}",
            CheckedValue.ofException(new NullPointerException("foo")).toString());
    }

    @Test
    void testEqualsHashCode() {
        var fooVal = CheckedValue.ofValue("foo");
        var fooVal2 = CheckedValue.ofValue("foo");
        var oneVal = CheckedValue.ofValue(1);
        var errBar = CheckedValue.ofException(new NullPointerException("bar"));
        var errFoo = CheckedValue.ofException(new NullPointerException("foo"));

        assertFalse(fooVal.equals(null));
        assertFalse(fooVal.equals("foo"));
        assertTrue(fooVal.equals(fooVal));
        assertTrue(fooVal.equals(fooVal2));
        assertFalse(fooVal.equals(oneVal));
        assertEquals(fooVal.hashCode(), fooVal2.hashCode());

        assertFalse(errFoo.equals(null));
        assertFalse(errFoo.equals("foo"));
        assertTrue(errFoo.equals(errFoo));

        assertFalse(errBar.equals(errFoo));
    }

    @Test
    void testIfPresent() {
        String foo = "foo";
        @SuppressWarnings("unchecked")
        Consumer<Object> consumer = mock(Consumer.class);
        doNothing().when(consumer).accept(any(Object.class));
        CheckedValue.ofValue(foo).ifPresent(consumer);
        verify(consumer).accept(foo);
    }

    @Test
    void testThrowableIfPresent() {
        @SuppressWarnings("unchecked")
        Consumer<Object> consumer = mock(Consumer.class);
        doNothing().when(consumer).accept(any(Object.class));
        CheckedValue.ofException(new NullPointerException()).ifPresent(consumer);
        verifyNoInteractions(consumer);
    }

    @Test
    void testOrElse() {
        String foo = "foo";
        String bar = "bar";
        assertSame(foo, CheckedValue.ofValue(foo).orElse(bar));
        assertSame(bar, CheckedValue.ofException(new NullPointerException()).orElse(bar));
    }

    @Test
    void testMap() {
        String foo = "foo";
        String bar = "bar";
        CheckedValue<Object, ?> errVal = CheckedValue.ofValue(foo);
        @SuppressWarnings("unchecked")
        Function<Object, Object> mapper = mock(Function.class);
        doReturn(bar).when(mapper).apply(any(Object.class));
        assertSame(bar, errVal.map(mapper).get());
        verify(mapper).apply(foo);
    }

    @Test
    void testExceptionMap() {
        var errVal = CheckedValue.ofException(new NullPointerException());
        @SuppressWarnings("unchecked")
        Function<Object, Object> mapper = mock(Function.class);
        doReturn(null).when(mapper).apply(any(Object.class));
        assertSame(errVal, errVal.map(mapper));
        verifyNoInteractions(mapper);
    }

    @Test
    void testOrElseGet() {
        String foo = "foo";
        @SuppressWarnings("unchecked")
        Supplier<String> supplier = mock(Supplier.class);
        doReturn(null).when(supplier).get();
        assertSame(foo, CheckedValue.ofValue(foo).orElseGet(supplier));
        verifyNoInteractions(supplier);
    }

    @Test
    void testExceptionOrElseGet() {
        String bar = "bar";
        @SuppressWarnings("unchecked")
        Supplier<Object> supplier = mock(Supplier.class);
        doReturn(bar).when(supplier).get();

        assertSame(bar, CheckedValue.ofException(new NullPointerException()).orElseGet(supplier));
        verify(supplier).get();
    }
}
