/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Overall tests for {@link ContactEffectiveStatement.OptionalIn} and its ilk.
 */
class OptionalInEffectiveTest {
    @ParameterizedTest
    @MethodSource("classes")
    void emptyLookup(final StatementDefinition<?, ?, ?> def) {
        final var stmtClass = def.effectiveRepresentation();
        final var optionalInClass = optionalInClass(stmtClass);

        final var optionalIn = mock(optionalInClass, Answers.CALLS_REAL_METHODS);
        doReturn(List.of()).when(optionalIn).effectiveSubstatements();
        assertNull(lookupMethod(stmtClass, optionalInClass).apply(optionalIn));
        assertEquals(Optional.empty(), findMethod(stmtClass, optionalInClass).apply(optionalIn));

        final var getMethod = getMethod(stmtClass, optionalInClass);
        final var ex = assertThrows(NoSuchElementException.class, () -> getMethod.apply(optionalIn));
        assertEquals("No " + def.simpleName() + " statement present in " + optionalIn, ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("classes")
    void otherLookup(final StatementDefinition<?, ?, ?> def) {
        final var stmtClass = def.effectiveRepresentation();
        final var optionalInClass = optionalInClass(stmtClass);

        final var optionalIn = mock(optionalInClass, Answers.CALLS_REAL_METHODS);
        doReturn(List.of(mock(EffectiveStatement.class))).when(optionalIn).effectiveSubstatements();
        assertNull(lookupMethod(stmtClass, optionalInClass).apply(optionalIn));
        assertEquals(Optional.empty(), findMethod(stmtClass, optionalInClass).apply(optionalIn));
    }

    @ParameterizedTest
    @MethodSource("classes")
    void lookup(final StatementDefinition<?, ?, ?> def) {
        final var stmtClass = def.effectiveRepresentation();
        final var optionalInClass = optionalInClass(stmtClass);

        final var stmt = mock(stmtClass, Answers.CALLS_REAL_METHODS);
        final var optionalIn = mock(optionalInClass, Answers.CALLS_REAL_METHODS);
        doReturn(List.of(stmt)).when(optionalIn).effectiveSubstatements();
        assertSame(stmt, lookupMethod(stmtClass, optionalInClass).apply(optionalIn));
        assertEquals(Optional.of(stmt), findMethod(stmtClass, optionalInClass).apply(optionalIn));
        assertSame(stmt, getMethod(stmtClass, optionalInClass).apply(optionalIn));
    }

    @SuppressWarnings("rawtypes")
    private static Class<? extends EffectiveStatement> optionalInClass(
            final Class<? extends EffectiveStatement<?, ?>> stmtClass) {
        return Arrays.stream(stmtClass.getDeclaredClasses())
            .filter(cls -> "OptionalIn".equals(cls.getSimpleName().replace("EffectiveStatement", "Statement")))
            .findFirst()
            .orElseThrow()
            .asSubclass(EffectiveStatement.class);
    }

    private static <S extends EffectiveStatement<?, ?>, O extends EffectiveStatement<?, ?>>
            Function<Object, S> lookupMethod(final Class<S> stmtClass, final Class<O> optionalInClass) {
        assertSame(stmtClass, optionalInClass.getEnclosingClass());

        final var methodNameChars = stmtClass.getSimpleName().replace("EffectiveStatement", "Statement").toCharArray();
        methodNameChars[0] = Character.toLowerCase(methodNameChars[0]);
        final var methodName = new String(methodNameChars);

        final var method = assertDoesNotThrow(() -> optionalInClass.getDeclaredMethod(methodName));
        assertSame(stmtClass, method.getReturnType());

        return self -> stmtClass.cast(assertDoesNotThrow(() -> method.invoke(self)));
    }

    private static <S extends EffectiveStatement<?, ?>, O extends EffectiveStatement<?, ?>>
            Function<Object, Optional<S>> findMethod(final Class<S> stmtClass, final Class<O> optionalInClass) {
        assertSame(stmtClass, optionalInClass.getEnclosingClass());

        final var method = assertDoesNotThrow(() -> optionalInClass.getDeclaredMethod(
            "find" + stmtClass.getSimpleName().replace("EffectiveStatement", "Statement")));
        assertEquals(Optional.class,  method.getReturnType());

        return self -> ((Optional<?>) assertDoesNotThrow(() -> method.invoke(self))).map(stmtClass::cast);
    }

    @SuppressWarnings("checkstyle:avoidHidingCauseException")
    private static <S extends EffectiveStatement<?, ?>, O extends EffectiveStatement<?, ?>>
            Function<Object, S> getMethod(final Class<S> stmtClass, final Class<O> optionalInClass) {
        assertSame(stmtClass, optionalInClass.getEnclosingClass());

        final var method = assertDoesNotThrow(() -> optionalInClass.getDeclaredMethod(
            "get" + stmtClass.getSimpleName().replace("EffectiveStatement", "Statement")));
        assertSame(stmtClass, method.getReturnType());

        return self -> {
            final Object obj;
            try {
                obj = method.invoke(self);
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            } catch (InvocationTargetException e) {
                throw assertInstanceOf(RuntimeException.class, e.getCause());
            }
            return stmtClass.cast(obj);
        };
    }

    private static List<Arguments> classes() {
        return List.of(
            arguments(ContactStatement.DEF),
            arguments(DescriptionStatement.DEF),
            arguments(KeyStatement.DEF),
            arguments(MaxElementsStatement.DEF),
            arguments(MinElementsStatement.DEF),
            arguments(OrderedByStatement.DEF),
            arguments(PresenceStatement.DEF),
            arguments(ReferenceStatement.DEF),
            arguments(UnitsStatement.DEF));
    }
}
