/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.repo.api.ResolverSupportedFeatures;

public class SupportedFeatureSet extends AbstractSet<QName> {

    protected Map<QNameModule, Set<Set<QName>>> moduleMap = new HashMap<>();

    @Override
    public UnmodifiableIterator<QName> iterator() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return moduleMap.isEmpty();
    }

    @Override
    public boolean contains(Object other) {
        if (other instanceof QName qname) {
            if (moduleMap.containsKey(qname.getModule())) {
                final Set<Set<QName>> sets = moduleMap.get(qname.getModule());
                for (Set<QName> set : sets) {
                    if (set.contains(qname)) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        }
        throw new IllegalArgumentException("Argument must be a QName.");
    }

    synchronized void add(QNameModule module, Set<QName> features) {
        if (!moduleMap.containsKey(module)) {
            moduleMap.put(module, new HashSet<>());
        }
        moduleMap.get(module).add(ImmutableSet.copyOf(features));
    }

    synchronized void remove(QNameModule module, Set<QName> features) {
        final Set<Set<QName>> featuresSet = moduleMap.get(module);
        featuresSet.remove(features);

        if (featuresSet.isEmpty()) {
            moduleMap.remove(module);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof SupportedFeatureSet) {
            return hashCode() == other.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return moduleMap.hashCode();
    }

    synchronized ImmutableFeatureSet getImmutableCopy() {
        return new ImmutableFeatureSet(this);
    }

    static final class ImmutableFeatureSet extends SupportedFeatureSet implements ResolverSupportedFeatures {
        private ImmutableFeatureSet(SupportedFeatureSet of) {
            for (QNameModule module : of.moduleMap.keySet()) {
                final Set<Set<QName>> setOfSets = new HashSet<>();
                for (Set<QName> set : of.moduleMap.get(module)) {
                    setOfSets.add(ImmutableSet.copyOf(set));
                }
                moduleMap.put(module, ImmutableSet.copyOf(setOfSets));
            }
            moduleMap = ImmutableMap.copyOf(moduleMap);
        }
    }
}
