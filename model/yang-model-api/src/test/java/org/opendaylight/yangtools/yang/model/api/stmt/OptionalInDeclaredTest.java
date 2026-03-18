/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Overall tests for {@link ArgumentStatement.OptionalIn} and its ilk.
 */
class OptionalInDeclaredTest {
    @ParameterizedTest
    @MethodSource("classes")
    void emptyLookup(final Class<? extends DeclaredStatement<?>> stmtClass,
            final Class<? extends DeclaredStatement<?>> optionalInClass) {
        final var optionalIn = mock(optionalInClass, Answers.CALLS_REAL_METHODS);
        doReturn(List.of()).when(optionalIn).declaredSubstatements();
        assertNull(lookupMethod(stmtClass, optionalInClass).apply(optionalIn));
        assertEquals(Optional.empty(), findMethod(stmtClass, optionalInClass).apply(optionalIn));

        final var getMethod = getMethod(stmtClass, optionalInClass);
        final var ex = assertThrows(NoSuchElementException.class, () -> getMethod.apply(optionalIn));
        assertThat(ex.getMessage()).startsWith("No ").endsWith(" statement present in " + optionalIn);
    }

    @ParameterizedTest
    @MethodSource("classes")
    void otherLookup(final Class<? extends DeclaredStatement<?>> stmtClass,
            final Class<? extends DeclaredStatement<?>> optionalInClass) {
        final var optionalIn = mock(optionalInClass, Answers.CALLS_REAL_METHODS);
        doReturn(List.of(mock(DeclaredStatement.class))).when(optionalIn).declaredSubstatements();
        assertNull(lookupMethod(stmtClass, optionalInClass).apply(optionalIn));
        assertEquals(Optional.empty(), findMethod(stmtClass, optionalInClass).apply(optionalIn));
    }

    @ParameterizedTest
    @MethodSource("classes")
    void lookup(final Class<? extends DeclaredStatement<?>> stmtClass,
            final Class<? extends DeclaredStatement<?>> optionalInClass) {
        final var stmt = mock(stmtClass, Answers.CALLS_REAL_METHODS);
        final var optionalIn = mock(optionalInClass, Answers.CALLS_REAL_METHODS);
        doReturn(List.of(stmt)).when(optionalIn).declaredSubstatements();
        assertSame(stmt, lookupMethod(stmtClass, optionalInClass).apply(optionalIn));
        assertEquals(Optional.of(stmt), findMethod(stmtClass, optionalInClass).apply(optionalIn));
        assertSame(stmt, getMethod(stmtClass, optionalInClass).apply(optionalIn));
    }

    private static <S extends DeclaredStatement<?>, O extends DeclaredStatement<?>> Function<Object, S> lookupMethod(
            final Class<S> stmtClass, final Class<O> optionalInClass) {
        assertSame(stmtClass, optionalInClass.getEnclosingClass());

        final var methodNameChars = stmtClass.getSimpleName().toCharArray();
        methodNameChars[0] = Character.toLowerCase(methodNameChars[0]);
        final var methodName = new String(methodNameChars);

        final var method = assertDoesNotThrow(() -> optionalInClass.getDeclaredMethod(methodName));
        assertSame(stmtClass, method.getReturnType());

        return self -> stmtClass.cast(assertDoesNotThrow(() -> method.invoke(self)));
    }

    private static <S extends DeclaredStatement<?>, O extends DeclaredStatement<?>>
            Function<Object, Optional<S>> findMethod(final Class<S> stmtClass, final Class<O> optionalInClass) {
        assertSame(stmtClass, optionalInClass.getEnclosingClass());

        final var method = assertDoesNotThrow(
            () -> optionalInClass.getDeclaredMethod("find" + stmtClass.getSimpleName()));
        assertEquals(Optional.class,  method.getReturnType());

        return self -> ((Optional<?>) assertDoesNotThrow(() -> method.invoke(self))).map(stmtClass::cast);
    }

    @SuppressWarnings("checkstyle:avoidHidingCauseException")
    private static <S extends DeclaredStatement<?>, O extends DeclaredStatement<?>> Function<Object, S> getMethod(
            final Class<S> stmtClass, final Class<O> optionalInClass) {
        assertSame(stmtClass, optionalInClass.getEnclosingClass());

        final var method = assertDoesNotThrow(
            () -> optionalInClass.getDeclaredMethod("get" + stmtClass.getSimpleName()));
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
            arguments(ArgumentStatement.class, ArgumentStatement.OptionalIn.class),
            arguments(BelongsToStatement.class, BelongsToStatement.OptionalIn.class),
            arguments(ConfigStatement.class, ConfigStatement.OptionalIn.class),
            arguments(ContactStatement.class, ContactStatement.OptionalIn.class),
            arguments(DefaultStatement.class, DefaultStatement.OptionalIn.class),
            arguments(DescriptionStatement.class, DescriptionStatement.OptionalIn.class),
            arguments(ErrorAppTagStatement.class, ErrorAppTagStatement.OptionalIn.class),
            arguments(ErrorMessageStatement.class, ErrorMessageStatement.OptionalIn.class),
            arguments(FractionDigitsStatement.class, FractionDigitsStatement.OptionalIn.class),
            arguments(InputStatement.class, InputStatement.OptionalIn.class),
            arguments(KeyStatement.class, KeyStatement.OptionalIn.class),
            arguments(LengthStatement.class, LengthStatement.OptionalIn.class),
            arguments(MandatoryStatement.class, MandatoryStatement.OptionalIn.class),
            arguments(MaxElementsStatement.class, MaxElementsStatement.OptionalIn.class),
            arguments(MinElementsStatement.class, MinElementsStatement.OptionalIn.class),
            arguments(ModifierStatement.class, ModifierStatement.OptionalIn.class),
            arguments(NamespaceStatement.class, NamespaceStatement.OptionalIn.class),
            arguments(OrderedByStatement.class, OrderedByStatement.OptionalIn.class),
            arguments(OrganizationStatement.class, OrganizationStatement.OptionalIn.class),
            arguments(OutputStatement.class, OutputStatement.OptionalIn.class),
            arguments(PathStatement.class, PathStatement.OptionalIn.class),
            arguments(PositionStatement.class, PositionStatement.OptionalIn.class),
            arguments(PrefixStatement.class, PrefixStatement.OptionalIn.class),
            arguments(PresenceStatement.class, PresenceStatement.OptionalIn.class),
            arguments(RangeStatement.class, RangeStatement.OptionalIn.class),
            arguments(ReferenceStatement.class, ReferenceStatement.OptionalIn.class),
            arguments(RequireInstanceStatement.class, RequireInstanceStatement.OptionalIn.class),
            arguments(RevisionDateStatement.class, RevisionDateStatement.OptionalIn.class),
            arguments(StatusStatement.class, StatusStatement.OptionalIn.class),
            arguments(TypeStatement.class, TypeStatement.OptionalIn.class),
            arguments(UnitsStatement.class, UnitsStatement.OptionalIn.class),
            arguments(ValueStatement.class, ValueStatement.OptionalIn.class),
            arguments(WhenStatement.class, WhenStatement.OptionalIn.class),
            arguments(YangVersionStatement.class, YangVersionStatement.OptionalIn.class),
            arguments(YinElementStatement.class, YinElementStatement.OptionalIn.class));
    }
}
