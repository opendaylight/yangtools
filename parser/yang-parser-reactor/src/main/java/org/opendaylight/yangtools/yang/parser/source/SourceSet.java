/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;

/**
 * A set of sources which constitute a single {@code module} and all its {@code submodule}s.
 */
@NonNullByDefault
public sealed interface SourceSet extends Immutable permits DefaultSourceSet {
    /**
     * {@return this set's module}
     */
    SourceInfoRef.OfModule module();

    /**
     * {@return this set's submodules}
     */
    // TODO: improve the ordering contract here to guarantee the submodules are dependency-ordered, i.e. an included
    //       submodule is encountered before any of the submodules including it. Secondary order should be based on
    //       SourceIdentifier comparison
    List<SourceInfoRef.OfSubmodule> submodules();

    /**
     * Return a new {@link Builder} for a {@link SourceSet} centered around a module.
     *
     * @param module the module
     * @return the builder
     */
    static Builder builder(final SourceInfoRef.OfModule module) {
        return new Builder(module);
    }

    /**
     * A builder for {@link SourceSet} instances. Enforces some amount of consistency.
     */
    // TODO: This builder does not enforce RFC7950 requirement for module to 'include' all sts submodules.
    //       Introduce a StrictBuilder, which will enforce that invariant, reporting a checked exception if that ever
    //       happens.
    final class Builder implements Mutable {
        // the comparator we use to order the list of submodules
        private static final Comparator<SourceInfoRef.OfSubmodule> SUBMODULE_SORT = (ref1, ref2) -> {
            final var id1 = ref1.info().sourceId();
            final var id2 = ref2.info().sourceId();
            final int cmp = id1.name().compareTo(id2.name());
            return cmp != 0 ? cmp : Revision.compare(id1.revision(), id2.revision());
        };

        private final HashMap<SourceIdentifier, SourceInfoRef.OfSubmodule> submodules = new HashMap<>();
        private final SourceInfoRef.OfModule module;

        Builder(final SourceInfoRef.OfModule module) {
            this.module = requireNonNull(module);
        }

        /**
         * Add a submodule to the {@link SourceSet} being built.
         *
         * @param submodule the submodule
         * @return this builder
         * @throws IllegalArgumentException if the submodule cannot be added to this set
         */
        public Builder addSubmodule(final SourceInfoRef.OfSubmodule submodule) {
            final var info = submodule.info();
            if (!info.belongsTo().isSatisfiedBy(module.info().sourceId())) {
                throw new IllegalArgumentException(submodule + " does not belong to " + module);
            }

            final var prev = submodules.putIfAbsent(info.sourceId(), submodule);
            if (prev != null) {
                throw new IllegalArgumentException(submodule + " conflicts with " + prev);
            }

            return this;
        }

        /**
         * {@return a new {@link SourceSet} based on the state accumulated in this builder}
         * @throws IllegalStateException if the state is not consistent
         */
        public SourceSet build() {
            checkAllIncludesSatisfied(module.info());
            for (var submodule : submodules.values()) {
                checkAllIncludesSatisfied(submodule.info());
            }

            return new DefaultSourceSet(module, submodules.values().stream()
                .sorted(SUBMODULE_SORT)
                .collect(Collectors.toUnmodifiableList()));
        }

        private void checkAllIncludesSatisfied(final SourceInfo info) {
            for (var include : info.includes()) {
                if (isUnsatisfied(include)) {
                    throw new IllegalStateException(include + " from " + info.sourceId() + " is not satisfied");
                }
            }
        }

        private boolean isUnsatisfied(final Include include) {
            for (var sourceId : submodules.keySet()) {
                if (include.isSatisfiedBy(sourceId)) {
                    return false;
                }
            }
            return true;
        }
    }
}
