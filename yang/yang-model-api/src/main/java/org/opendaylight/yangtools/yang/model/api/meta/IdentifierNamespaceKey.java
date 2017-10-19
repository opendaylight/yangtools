/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Abstract base class for {@link IdentifierNamespace}s which require flexible lookups on identifiers. This is
 * an optional extension, sitting outside of IdentifierNamespace and wraps each stored namespace key with an instance
 * of {@link Exact}, which does not allow for null keys. Loose matches are allowed by looking up a specific subclass
 * of {@link Loose} class.
 *
 * @param <K> Embedded key type
 *
 * @author Robert Varga
 */
@Beta
public abstract class IdentifierNamespaceKey<K> {
    /**
     * A loose key matcher instance. Implementations of this interface are responsible for performing the actual
     * key matching associated with loose keys. For each candidate key, an implementation should return
     * a {@link KeyMatcherDecision}. Implementations can be stateful and
     *
     * @param <K> Key type
     */
    @FunctionalInterface
    public interface KeyMatcher<K> extends Function<K, KeyMatcherDecision> {

    }

    /**
     * Key matching decision made by a {@link KeyMatcher}.
     */
    public enum KeyMatcherDecision {
        /**
         * Supplied key was accepted and is better than the previous match.
         */
        ACCEPT,
        /**
         * Supplied key was rejected, any previous match is still the best match so far.
         */
        REJECT,
        /**
         * Supplied key was accepted and it should be the result of this match operation, e.g. no further matching
         * should be done.
         */
        RESULT,
    }

    /**
     * A loose match wrapper.
     *
     * @param <K> Matching key type
     */
    public static final class Loose<K> extends IdentifierNamespaceKey<K> {
        private final Supplier<KeyMatcher<K>> matcherSupplier;

        Loose(final Supplier<KeyMatcher<K>> matcherSupplier) {
            this.matcherSupplier = requireNonNull(matcherSupplier);
        }

        /**
         * Instantiate a new matcher, which will be fed candidate keys into its {@link KeyMatcher#apply(Object)} method.
         *
         * @return A key matcher instance.
         */
        public KeyMatcher<K> createMatcher() {
            return matcherSupplier.get();
        }
    }

    /**
     * An exact key wrapper. Does not allow the key to be null.
     *
     * @param <K> Embedded key type
     */
    private static final class Exact<K> extends IdentifierNamespaceKey<K> {
        private final K key;

        Exact(final K key) {
            this.key = requireNonNull(key);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }

            return obj instanceof Exact && key.equals(((Exact<?>) obj).key);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("key", key).toString();
        }
    }

    IdentifierNamespaceKey() {
        // Hidden to prevent subclassing from outside
    }

    public static <K> IdentifierNamespaceKey<K> exact(final K key) {
        return new Exact<>(key);
    }

    public static <K> IdentifierNamespaceKey<K> loose(final Supplier<KeyMatcher<K>> matcherSupplier) {
        return new Loose<>(matcherSupplier);
    }
}
