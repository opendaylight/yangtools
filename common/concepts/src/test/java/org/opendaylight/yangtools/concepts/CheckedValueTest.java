/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

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
    public void testExceptionGet() {
        CheckedValue.ofException(new Exception()).get();
    }

    @Test(expected = IllegalStateException.class)
    public void testValueException() {
        CheckedValue.ofValue("foo").getException();
    }

    @Test
    public void testGet() {
        final String value = "foo";
        final CheckedValue<String, ?> val = CheckedValue.ofValue(value);
        assertTrue(val.isPresent());
        assertSame(value, val.get());
    }

    @Test
    public void testThrowableGetThrowable() {
        final Exception cause = new Exception();
        final CheckedValue<?, ?> val = CheckedValue.ofException(cause);
        assertFalse(val.isPresent());
        assertSame(cause, val.getException());
    }

    @Test
    public void testToString() {
        assertEquals("CheckedValue{first=foo}", CheckedValue.ofValue("foo").toString());
        assertEquals("CheckedValue{second=java.lang.NullPointerException: foo}",
            CheckedValue.ofException(new NullPointerException("foo")).toString());
    }

    @Test
    public void testEqualsHashCode() {
        final CheckedValue<String, ?> fooVal = CheckedValue.ofValue("foo");
        final CheckedValue<String, ?> fooVal2 = CheckedValue.ofValue("foo");
        final CheckedValue<Integer, ?> oneVal = CheckedValue.ofValue(1);
        final CheckedValue<?, ?> errBar = CheckedValue.ofException(new NullPointerException("bar"));
        final CheckedValue<?, ?> errFoo = CheckedValue.ofException(new NullPointerException("foo"));
        final CheckedValue<?, ?> errFoo2 = CheckedValue.ofException(new NullPointerException("foo"));

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
    public void testIfPresent() {
        final String foo = "foo";
        final Consumer<Object> consumer = mock(Consumer.class);
        doNothing().when(consumer).accept(any(Object.class));
        CheckedValue.ofValue(foo).ifPresent(consumer);
        verify(consumer).accept(foo);
    }

    @Test
    public void testThrowableIfPresent() {
        final Consumer<Object> consumer = mock(Consumer.class);
        doNothing().when(consumer).accept(any(Object.class));
        CheckedValue.ofException(new NullPointerException()).ifPresent(consumer);
        verifyZeroInteractions(consumer);
    }

    @Test
    public void testOrElse() {
        final String foo = "foo";
        final String bar = "bar";
        assertSame(foo, CheckedValue.ofValue(foo).orElse(bar));
        assertSame(bar, CheckedValue.ofException(new NullPointerException()).orElse(bar));
    }

    @Test
    public void testMap() {
        final String foo = "foo";
        final String bar = "bar";
        final CheckedValue<Object, ?> errVal = CheckedValue.ofValue(foo);
        final Function<Object, Object> mapper = mock(Function.class);
        doReturn(bar).when(mapper).apply(any(Object.class));
        assertSame(bar, errVal.map(mapper).get());
        verify(mapper).apply(foo);
    }

    @Test
    public void testExceptionMap() {
        final CheckedValue<Object, ?> errVal = CheckedValue.ofException(new NullPointerException());
        final Function<Object, Object> mapper = mock(Function.class);
        doReturn(null).when(mapper).apply(any(Object.class));
        assertSame(errVal, errVal.map(mapper));
        verifyZeroInteractions(mapper);
    }

    @Test
    public void testOrElseThrow() {
        final String foo = "foo";
        assertSame(foo, CheckedValue.ofValue(foo)
            .orElseThrow((Supplier<NullPointerException>)NullPointerException::new));
    }

    @Test(expected = InterruptedException.class)
    public void testThrowableOrElseThrow() throws InterruptedException {
        final String foo = "foo";
        final Exception cause = new NullPointerException(foo);
        CheckedValue.ofException(cause).orElseThrow((Supplier<InterruptedException>)InterruptedException::new);
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
    public void testExceptionOrElseGet() {
        final String bar = "bar";
        final Supplier<Object> supplier = mock(Supplier.class);
        doReturn(bar).when(supplier).get();

        assertSame(bar, CheckedValue.ofException(new NullPointerException()).orElseGet(supplier));
        verify(supplier).get();
    }
}
