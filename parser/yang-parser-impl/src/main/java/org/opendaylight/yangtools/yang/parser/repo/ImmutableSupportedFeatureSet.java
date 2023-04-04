/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.repo.api.SupportedFeatureSet;

final class ImmutableSupportedFeatureSet implements SupportedFeatureSet {
    // Note: not a ImmutableSetMultimap because we need to distinguish non-presence vs. empty Set
    private final ImmutableMap<QNameModule, ImmutableSet<String>> featuresByModule;

    ImmutableSupportedFeatureSet(final ImmutableMap<QNameModule, ImmutableSet<String>> featuresByModule) {
        this.featuresByModule = requireNonNull(featuresByModule);
    }

    @Override
    public boolean contains(final QName qname) {
        final var sets = featuresByModule.get(qname.getModule());
        return sets == null || sets.contains(qname.getLocalName());
    }

    @Override
    public int hashCode() {
        return featuresByModule.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof ImmutableSupportedFeatureSet other
            && featuresByModule.equals(other.featuresByModule);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("features", featuresByModule).toString();
    }
}
