/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for objects which are string-equivalent to canonical string representation specified
 * in a YANG model. Note that each subclass of {@link DerivedString} defines its own {@link #hashCode()} and
 * {@link #equals(Object)} contracts based on implementation particulars.
 *
 * <p>
 * Since YANG validation works on top of strings, which in itself is expensive and this class provides storage which
 * is potentially not based on strings, its design combines 'representation' and 'validated to match constraints'
 * aspects of a YANG type derived from string. To achieve that it cooperates with {@link DerivedStringValidator} and
 * {@link DerivedStringSupport}.
 *
 * <p>
 * Given the following YANG snippet:
 * <pre>
 *     typedef foo {
 *         type string;
 *         pattern "[1-9]?[0-9]";
 *     }
 *
 *     typedef bar {
 *         type foo;
 *         patter "[1-9][0-9]";
 *     }
 *
 *     typedef baz {
 *         type foo;
 *     }
 * </pre>
 * it is obvious we could use a storage class with 'int' as the internal representation of all three types and define
 * operations on top of it. In this case we would define:
 * <ul>
 *     <li>{@code public class FooDerivedString extends DerivedString<FooDerivedString>}, which implements all abstract
 *         methods of {@link DerivedString} as final methods. It will notably not override {@link #validator()} and
 *         must not be final.</li>
 *     <li>{@code public final class FooDerivedStringSupport extends DerivedStringSupport<FooDerivedString>}, which
 *         forms the baseline validator and instantiation for {@code FooDerivedString}. It should be a singleton class
 *         with a getInstance() method.</li>
 *     <li>{@code public class BarDerivedString extends FooDerivedString}, which overrides {@link #validator()} to
 *         indicate its contents have been validated to conform to bar -- it does that by returning the singleton
 *         instance of {@code BarDerivedStringValidator}.
 *     <li>{@code public final class BarDerivedStringValidator extends DerivedStringValidator<FooDerivedString,
 *         BarDerivedString}. This method needs to notably implement
 *         {@link DerivedStringValidator#validateRepresentation(DerivedString)} to hand out BarDerivedString instances.
 *         This class needs to be a singleton with a getInstance() method, too.</li>
 * </ul>
 * Since {@code baz} is not defining any new restrictions, all instances of FooDerivedString are valid for it and we
 * do not have to define any additional support.
 *
 * <p>
 * It is important for {@link DerivedString} subclasses not to be final because any YANG type can be further extended
 * and adding a final class in that hierarchy would prevent a proper class from being defined.
 *
 * @param <R> derived string representation
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@ThreadSafe
public abstract class DerivedString<R extends DerivedString<R>> implements Comparable<R>, Immutable, Serializable {
    private static final class Validator extends ClassValue<Boolean> {
        private static final Logger LOG = LoggerFactory.getLogger(Validator.class);

        @Override
        protected Boolean computeValue(final @Nullable Class<?> type) {
            // Every DerivedString representation class must:
            checkArgument(DerivedString.class.isAssignableFrom(type), "%s is not a DerivedString", type);

            // be non-final and public
            final int modifiers = type.getModifiers();
            checkArgument(Modifier.isPublic(modifiers), "%s must be public", type);
            checkArgument(!Modifier.isFinal(modifiers), "%s must not be final", type);

            // have at least one public or protected constructor (for subclasses)
            checkArgument(Arrays.stream(type.getDeclaredConstructors()).mapToInt(Constructor::getModifiers)
                .anyMatch(mod -> Modifier.isProtected(mod) || Modifier.isPublic(mod)),
                "%s must declare at least one protected or public constructor", type);

            try {
                // have a non-final non-abstract validator() method
                final int validator;
                try {
                    validator = type.getMethod("validator").getModifiers();
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException(type + " must have a non-abstract non-final validator() method",
                        e);
                }
                checkArgument(Modifier.isAbstract(validator), "%s must not have abstract validator()");
                checkArgument(!Modifier.isFinal(validator), "%s must not have final validator()");

                // have final toCanonicalString(), support(), hashCode() and equals(Object), compare(T) methods
                checkFinalMethod(type, "toCanonicalString");
                checkFinalMethod(type, "support");
                checkFinalMethod(type, "hashCode");
                checkFinalMethod(type, "equals", Object.class);
                checkFinalMethod(type, "compareTo", type);
            } catch (SecurityException e) {
                LOG.warn("Cannot completely validate {}", type, e);
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        }

        private static void checkFinalMethod(final Class<?> type, final String name) {
            try {
                checkFinalMethod(type.getMethod(name).getModifiers(), type, name, "");
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(type + " must have a final " + name + "() method", e);
            }
        }

        private static void checkFinalMethod(final Class<?> type, final String name, final Class<?> arg) {
            final String argName = arg.getSimpleName();
            try {
                checkFinalMethod(type.getMethod(name, arg).getModifiers(), type, name, argName);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(type + " must have a final " + name + "(" + argName + ") method", e);
            }
        }

        private static void checkFinalMethod(final int modifiers, final Class<?> type, final String name,
                final String args) {
            checkArgument(Modifier.isFinal(modifiers), "%s must have a final %s(%s) method", type, name, args);
        }
    }

    private static final ClassValue<Boolean> VALIDATED_REPRESENTATIONS = new Validator();
    private static final long serialVersionUID = 1L;

    /**
     * Return the canonical string representation of this object's value.
     *
     * @return Canonical string
     */
    public abstract String toCanonicalString();

    /**
     * Return the {@link DerivedStringSupport} associated with this type. It can be used to create new instances of this
     * representation.
     *
     * @return A {@link DerivedStringSupport} instance.
     */
    public abstract DerivedStringSupport<R> support();

    /**
     * Return a {@link DerivedStringValidator} associated with this value's validated type.
     *
     * @return A {@link DerivedStringValidator} instance.
     */
    public DerivedStringValidator<R, ? extends R> validator() {
        return support();
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public final String toString() {
        return toCanonicalString();
    }

    static <T extends DerivedString<?>> Class<T> validateRepresentationClass(final Class<T> representationClass) {
        // Validation is reflective, cache its result
        VALIDATED_REPRESENTATIONS.get(representationClass);
        return representationClass;
    }
}
