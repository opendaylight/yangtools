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
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;

/**
 * A set of sources which constitute a single {@code module} and all its {@code submodules}.
 */
@NonNullByDefault
public sealed interface SourceSet extends Immutable permits DefaultSourceSet {

    SourceInfoRef.OfModule module();

    List<SourceInfoRef.OfSubmodule> submodules();

    static Builder builder(final SourceInfoRef.OfModule module) {
        return new Builder(module);
    }

    /**
     * A builder for {@link SourceSet} instances. Enforces some amount of consistency.
     */
    final class Builder implements Mutable {
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

        public Builder addSubmodule(final SourceInfoRef.OfSubmodule submodule) {
            final var info = submodule.info();
            final var prev = submodules.putIfAbsent(info.sourceId(), submodule);
            if (prev != null) {
                throw new IllegalArgumentException(submodule + " conflicts with " + prev);
            }
            return this;
        }

        public SourceSet build() {
            // FIXME: validation
            return new DefaultSourceSet(module, submodules.values().stream()
                .sorted(SUBMODULE_SORT)
                .collect(Collectors.toUnmodifiableList()));
        }
    }
}
