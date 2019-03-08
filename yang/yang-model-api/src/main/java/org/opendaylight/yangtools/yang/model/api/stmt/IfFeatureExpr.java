/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A resolved {@code if-feature} expression, implementing a {@link Predicate}. Internal representation is that of
 * a tree of expressions, optimized for memory usage. {@link #negate()} performs an efficient logical negation without
 * relying on default predicate methods. Other Predicate methods, like {@link #and(Predicate)} are not optimized in
 * this implementation.
 *
 * <p>
 * The set of features referenced in this expression is available through {@link #getReferencedFeatures()}.
 *
 * @author Robert Varga
 */
@Beta
public abstract class IfFeatureExpr implements Immutable, Predicate<Set<QName>> {
    private abstract static class Single extends IfFeatureExpr {
        final QName qname;

        Single(final QName qname) {
            this.qname = requireNonNull(qname);
        }

        @Override
        public final ImmutableSet<QName> getReferencedFeatures() {
            return ImmutableSet.of(qname);
        }

        @Override
        public final int hashCode() {
            return qname.hashCode();
        }

        @Override
        final void addQNames(final Set<QName> set) {
            set.add(qname);
        }

        @Override
        public final boolean equals(final Object obj) {
            return this == obj || getClass().isInstance(obj) && qname.equals(((Single) obj).qname);
        }
    }

    // We are using arrays to hold our components to save a wee bit of space. The arrays originate from Sets retaining
    // insertion order of Lists, each component is guaranteed to be unique, in definition order, not appearing multiple
    // times
    private abstract static class AbstractArray<T> extends IfFeatureExpr {
        final T[] array;

        AbstractArray(final T[] array) {
            this.array = requireNonNull(array);
            verify(array.length > 1);
        }

        @Override
        public final int hashCode() {
            return Arrays.hashCode(array);
        }

        @Override
        public final boolean equals(final Object obj) {
            return this == obj || getClass().isInstance(obj)
                    && Arrays.deepEquals(array, ((AbstractArray<?>) obj).array);
        }

        abstract String infix();
    }

    private abstract static class Complex extends AbstractArray<IfFeatureExpr> {
        Complex(final IfFeatureExpr[] array) {
            super(array);
        }

        @Override
        public final Set<QName> getReferencedFeatures() {
            final Set<QName> ret = new HashSet<>();
            addQNames(ret);
            return ret;
        }

        @Override
        final void addQNames(final Set<QName> set) {
            for (IfFeatureExpr expr : array) {
                expr.addQNames(set);
            }
        }

        final IfFeatureExpr[] negateExprs() {
            final IfFeatureExpr[] ret = new IfFeatureExpr[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = verifyNotNull(array[i].negate());
            }
            return ret;
        }

        @Override
        public final String toString() {
            final StringBuilder sb = new StringBuilder("(");
            sb.append(array[0]);
            final String sep = infix();
            for (int i = 1; i < array.length; ++i) {
                sb.append(sep).append(array[i]);
            }
            return sb.append(')').toString();
        }
    }

    private abstract static class Compound extends AbstractArray<QName> {
        Compound(final QName[] qnames) {
            super(qnames);
        }

        @Override
        public final ImmutableSet<QName> getReferencedFeatures() {
            return ImmutableSet.copyOf(array);
        }

        @Override
        final void addQNames(final Set<QName> set) {
            set.addAll(Arrays.asList(array));
        }

        @Override
        public final String toString() {
            final StringBuilder sb = new StringBuilder();
            if (negated()) {
                sb.append("not ");
            }

            sb.append("(\"").append(array[0]).append('"');
            final String sep = infix();
            for (int i = 1; i < array.length; ++i) {
                sb.append(sep).append('"').append(array[i]).append('"');
            }
            return sb.append(')').toString();
        }

        abstract boolean negated();
    }

    private static final class Absent extends Single {
        Absent(final QName qname) {
            super(qname);
        }

        @Override
        public IfFeatureExpr negate() {
            return isPresent(qname);
        }

        @Override
        public boolean test(final Set<QName> supportedFeatures) {
            return !supportedFeatures.contains(qname);
        }

        @Override
        public String toString() {
            return "not \"" + qname + '"';
        }
    }

    private static final class Present extends Single {
        Present(final QName qname) {
            super(qname);
        }

        @Override
        public IfFeatureExpr negate() {
            return new Absent(qname);
        }

        @Override
        public boolean test(final Set<QName> supportedFeatures) {
            return supportedFeatures.contains(qname);
        }

        @Override
        public String toString() {
            return "\"" + qname + '"';
        }
    }

    private static final class AllExprs extends Complex {
        AllExprs(final IfFeatureExpr[] exprs) {
            super(exprs);
        }

        @Override
        public IfFeatureExpr negate() {
            return new AnyExpr(negateExprs());
        }

        @Override
        public boolean test(final Set<QName> supportedFeatures) {
            for (IfFeatureExpr expr : array) {
                if (!expr.test(supportedFeatures)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        String infix() {
            return " and ";
        }
    }

    private static final class AnyExpr extends Complex {
        AnyExpr(final IfFeatureExpr[] exprs) {
            super(exprs);
        }

        @Override
        public IfFeatureExpr negate() {
            return new AllExprs(negateExprs());
        }

        @Override
        public boolean test(final Set<QName> supportedFeatures) {
            for (IfFeatureExpr expr : array) {
                if (expr.test(supportedFeatures)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        String infix() {
            return " or ";
        }
    }

    private abstract static class AbstractAll extends Compound {
        AbstractAll(final QName[] qnames) {
            super(qnames);
        }

        @Override
        public final boolean test(final Set<QName> supportedFeatures) {
            final boolean neg = negated();
            for (QName qname : array) {
                if (supportedFeatures.contains(qname) == neg) {
                    return false;
                }
            }
            return true;
        }

        @Override
        final String infix() {
            return " and ";
        }
    }

    private static final class All extends AbstractAll {
        All(final QName[] qnames) {
            super(qnames);
        }

        @Override
        public IfFeatureExpr negate() {
            return new NotAll(array);
        }

        @Override
        boolean negated() {
            return false;
        }
    }

    private static final class NotAll extends AbstractAll {
        NotAll(final QName[] qnames) {
            super(qnames);
        }

        @Override
        public IfFeatureExpr negate() {
            return new All(array);
        }

        @Override
        boolean negated() {
            return true;
        }
    }

    private abstract static class AbstractAny extends Compound {
        AbstractAny(final QName[] qnames) {
            super(qnames);
        }

        @Override
        public final boolean test(final Set<QName> supportedFeatures) {
            for (QName qname : array) {
                if (supportedFeatures.contains(qname)) {
                    return !negated();
                }
            }
            return negated();
        }

        @Override
        final String infix() {
            return " or ";
        }
    }

    private static final class Any extends AbstractAny {
        Any(final QName[] array) {
            super(array);
        }

        @Override
        public IfFeatureExpr negate() {
            return new NotAny(array);
        }

        @Override
        boolean negated() {
            return false;
        }
    }

    private static final class NotAny extends AbstractAny {
        NotAny(final QName[] qnames) {
            super(qnames);
        }

        @Override
        public IfFeatureExpr negate() {
            return new Any(array);
        }

        @Override
        boolean negated() {
            return true;
        }
    }

    /**
     * Construct an assertion that a feature is present in the set passed to {@link #test(Set)}.
     *
     * @param qname Feature QName
     * @return An expression
     * @throws NullPointerException if {@code qname} is null
     */
    public static final @NonNull IfFeatureExpr isPresent(final QName qname) {
        return new Present(qname);
    }

    /**
     * Construct a intersection (logical {@code AND}) expression of specified expressions.
     *
     * @param exprs Constituent expressions
     * @return An expression
     * @throws NullPointerException if {@code exprs} or any of its members is null
     * @throws IllegalArgumentException if {@code exprs} is empty
     */
    public static final @NonNull IfFeatureExpr and(final Set<IfFeatureExpr> exprs) {
        checkArgument(!exprs.isEmpty(), "Expressions may not be empty");
        if (exprs.size() == 1) {
            return exprs.iterator().next();
        }
        final Boolean composition = composition(exprs);
        if (composition == null) {
            return new AllExprs(exprs.toArray(new IfFeatureExpr[0]));
        }

        final QName[] qnames = extractQNames(exprs);
        return composition ? new All(qnames) : new NotAny(qnames);
    }

    /**
     * Construct a union (logical {@code OR}) expression of specified expressions.
     *
     * @param exprs Constituent expressions
     * @return An expression
     * @throws NullPointerException if {@code exprs} or any of its members is null
     * @throws IllegalArgumentException if {@code exprs} is empty
     */
    public static final @NonNull IfFeatureExpr or(final Set<IfFeatureExpr> exprs) {
        checkArgument(!exprs.isEmpty(), "Expressions may not be empty");
        if (exprs.size() == 1) {
            return exprs.iterator().next();
        }
        final Boolean composition = composition(exprs);
        if (composition == null) {
            return new AnyExpr(exprs.toArray(new IfFeatureExpr[0]));
        }

        final QName[] qnames = extractQNames(exprs);
        return composition ? new Any(qnames) : new NotAll(qnames);
    }

    /**
     * Returns the set of all {@code feature}s referenced by this expression. Each feature is identified by its QName.
     *
     * @return The set of referenced features. Mutability of the returned Set and order of features is undefined.
     */
    public abstract @NonNull Set<QName> getReferencedFeatures();

    @Override
    public abstract @NonNull IfFeatureExpr negate();

    @Override
    public abstract boolean test(final Set<QName> supportedFeatures);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

    /**
     * Add QNames referenced by this expression into a target set.
     *
     * @param set The set to fill
     * @throws NullPointerException if {@code set} is null
     */
    abstract void addQNames(@NonNull Set<QName> set);

    private static Boolean composition(final Set<IfFeatureExpr> exprs) {
        boolean negative = false;
        boolean positive = false;
        for (IfFeatureExpr expr : exprs) {
            if (expr instanceof Present) {
                positive = true;
            } else if (expr instanceof Absent) {
                negative = true;
            } else {
                return null;
            }
        }

        verify(negative || positive, "Unresolved expressions %s", exprs);
        return positive == negative ? null : positive;
    }

    private static QName[] extractQNames(final Set<IfFeatureExpr> exprs) {
        return exprs.stream().map(expr -> {
            verify(expr instanceof Single, "Unexpected expression %s", expr);
            return ((Single) expr).qname;
        }).collect(ImmutableSet.toImmutableSet()).toArray(new QName[0]);
    }
}
