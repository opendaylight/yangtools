/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.Test;

public class CheckedValueTest {
    @Test(expected = NullPointerException.class)
    public void testNullValue() {
        CheckedValue.ofValue(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testErrorStringGet() {
        CheckedValue.ofErrorString("foo").get();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowableGet() {
        CheckedValue.ofThrowable(new Throwable()).get();
    }

    @Test(expected = IllegalStateException.class)
    public void testValueErrorString() {
        CheckedValue.ofValue("foo").getErrorString();
    }

    @Test(expected = IllegalStateException.class)
    public void testValueThrowable() {
        CheckedValue.ofValue("foo").getThrowable();
    }

    @Test
    public void testGet() {
        final String value = "foo";
        final CheckedValue<String> val = CheckedValue.ofValue(value);
        assertTrue(val.isPresent());
        assertSame(value, val.get());
    }

    @Test
    public void testThrowableGetErrorString() {
        final String error = "foo";
        final CheckedValue<?> val = CheckedValue.ofErrorString(error);
        assertFalse(val.isPresent());
        assertSame(error, val.getErrorString().get());
        assertEquals(Optional.empty(), val.getThrowable());
    }

    @Test
    public void testThrowableGetThrowable() {
        final Throwable cause = new Throwable();
        final CheckedValue<?> val = CheckedValue.ofThrowable(cause);
        assertFalse(val.isPresent());
        assertSame(cause, val.getThrowable().get());
        assertEquals(Optional.empty(), val.getErrorString());
    }

    @Test
    public void testToString() {
        assertEquals("CheckedValue{value=foo}", CheckedValue.ofValue("foo").toString());
        assertEquals("CheckedValue{error=foo}", CheckedValue.ofErrorString("foo").toString());
        assertEquals("CheckedValue{error=java.lang.NullPointerException: foo}",
            CheckedValue.ofThrowable(new NullPointerException("foo")).toString());
    }

    @Test
    public void testEqualsHashCode() {
        final CheckedValue<String> fooVal = CheckedValue.ofValue("foo");
        final CheckedValue<String> fooVal2 = CheckedValue.ofValue("foo");
        final CheckedValue<Integer> oneVal = CheckedValue.ofValue(1);
        final CheckedValue<?> errBar = CheckedValue.ofErrorString("bar");
        final CheckedValue<?> errFoo = CheckedValue.ofErrorString("foo");
        final CheckedValue<?> errFoo2 = CheckedValue.ofErrorString("foo");

        assertFalse(fooVal.equals(null));
        assertFalse(fooVal.equals("foo"));
        assertTrue(fooVal.equals(fooVal));
        assertTrue(fooVal.equals(fooVal2));
        assertFalse(fooVal.equals(oneVal));
        assertEquals(fooVal.hashCode(), fooVal2.hashCode());

        assertFalse(errFoo.equals(null));
        assertFalse(errFoo.equals("foo"));
        assertTrue(errFoo.equals(errFoo));
        assertTrue(errFoo.equals(errFoo2));
        assertEquals(errFoo.hashCode(), errFoo2.hashCode());

        assertFalse(errBar.equals(errFoo));
    }

    @Test
    public void testIfPresent() {
        final String foo = "foo";
        final Consumer<Object> consumer = mock(Consumer.class);
        doNothing().when(consumer).accept(any(Object.class));
        CheckedValue.ofValue(foo).ifPresent(consumer);
        verify(consumer).accept(foo);
    }

    @Test
    public void testErrorIfPresent() {
        final Consumer<Object> consumer = mock(Consumer.class);
        doNothing().when(consumer).accept(any(Object.class));
        CheckedValue.ofErrorString("foo").ifPresent(consumer);
        verifyZeroInteractions(consumer);
    }

    @Test
    public void testThrowableIfPresent() {
        final Consumer<Object> consumer = mock(Consumer.class);
        doNothing().when(consumer).accept(any(Object.class));
        CheckedValue.ofThrowable(new NullPointerException()).ifPresent(consumer);
        verifyZeroInteractions(consumer);
    }

    @Test
    public void testOrElse() {
        final String foo = "foo";
        final String bar = "bar";
        assertSame(foo, CheckedValue.ofValue(foo).orElse(bar));
        assertSame(bar, CheckedValue.ofErrorString(foo).orElse(bar));
        assertSame(bar, CheckedValue.ofThrowable(new NullPointerException()).orElse(bar));
    }

    @Test
    public void testMap() {
        final String foo = "foo";
        final String bar = "bar";
        final CheckedValue<Object> errVal = CheckedValue.ofValue(foo);
        final Function<Object, Object> mapper = mock(Function.class);
        doReturn(bar).when(mapper).apply(any(Object.class));
        assertSame(bar, errVal.map(mapper).get());
        verify(mapper).apply(foo);
    }

    @Test
    public void testErrorMap() {
        final CheckedValue<Object> errVal = CheckedValue.ofErrorString("foo");
        final Function<Object, Object> mapper = mock(Function.class);
        doReturn(null).when(mapper).apply(any(Object.class));
        assertSame(errVal, errVal.map(mapper));
        verifyZeroInteractions(mapper);
    }

    @Test
    public void testThrowableMap() {
        final CheckedValue<Object> errVal = CheckedValue.ofThrowable(new NullPointerException());
        final Function<Object, Object> mapper = mock(Function.class);
        doReturn(null).when(mapper).apply(any(Object.class));
        assertSame(errVal, errVal.map(mapper));
        verifyZeroInteractions(mapper);
    }

    @Test
    public void testOrElseThrow() {
        final String foo = "foo";
        assertSame(foo, CheckedValue.ofValue(foo).orElseThrow(NullPointerException::new));
    }

    @Test
    public void testErrorOrElseThrow() {
        final String foo = "foo";

        try {
            CheckedValue.ofErrorString(foo).orElseThrow(Exception::new);
            fail();
        } catch (Exception e) {
            assertSame(foo, e.getMessage());
        }
    }

    @Test
    public void testThrowableOrElseThrow() {
        final String foo = "foo";
        final Throwable cause = new NullPointerException(foo);

        try {
            CheckedValue.ofThrowable(cause).orElseThrow(Exception::new);
            fail();
        } catch (Exception e) {
            assertSame(foo, e.getMessage());
            assertArrayEquals(new Throwable[] { cause }, e.getSuppressed());
        }
    }

    @Test
    public void testOrElseGet() {
        final String foo = "foo";
        final Supplier<String> supplier = mock(Supplier.class);
        doReturn(null).when(supplier).get();
        assertSame(foo, CheckedValue.ofValue(foo).orElseGet(supplier));
        verifyZeroInteractions(supplier);
    }

    @Test
    public void testErrorOrElseGet() {
        final String foo = "foo";
        final String bar = "bar";
        final Supplier<Object> supplier = mock(Supplier.class);
        doReturn(bar).when(supplier).get();

        assertSame(bar, CheckedValue.ofErrorString(foo).orElseGet(supplier));
        verify(supplier).get();
    }

    @Test
    public void testThrowableOrElseGet() {
        final String bar = "bar";
        final Supplier<Object> supplier = mock(Supplier.class);
        doReturn(bar).when(supplier).get();

        assertSame(bar, CheckedValue.ofThrowable(new NullPointerException()).orElseGet(supplier));
        verify(supplier).get();
    }
}
