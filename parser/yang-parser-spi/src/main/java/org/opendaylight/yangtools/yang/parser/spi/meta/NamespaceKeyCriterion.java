/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * Namespace key matching criterion.
 *
 * @param <K> Key type
 */
@Beta
public abstract class NamespaceKeyCriterion<K> {
    private static final class LatestRevisionModule extends NamespaceKeyCriterion<SourceIdentifier> {
        private final Unqualified moduleName;

        LatestRevisionModule(final Unqualified moduleName) {
            this.moduleName = requireNonNull(moduleName);
        }

        @Override
        public boolean match(final SourceIdentifier key) {
            return moduleName.equals(key.name());
        }

        @Override
        public SourceIdentifier select(final SourceIdentifier first, final SourceIdentifier second) {
            return Revision.compare(first.revision(), second.revision()) >= 0 ? first : second;
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return toStringHelper.add("moduleName", moduleName.getLocalName());
        }
    }

    /**
     * Return a criterion which selects the latest known revision of a particular module.
     *
     * @param moduleName Module name
     * @return A criterion object.
     */
    public static NamespaceKeyCriterion<SourceIdentifier> latestRevisionModule(final Unqualified moduleName) {
        return new LatestRevisionModule(moduleName);
    }

    /**
     * Match a key against this criterion.
     *
     * @param key Key to be matched
     * @return True if the key matches this criterion, false otherwise.
     */
    public abstract boolean match(@NonNull K key);

    /**
     * Select the better match from two candidate keys.
     *
     * @param first First key
     * @param second Second key
     * @return Selected key, must be either first or second key, by identity.
     */
    public abstract K select(@NonNull K first, @NonNull K second);

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper;
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        return super.equals(obj);
    }
}
