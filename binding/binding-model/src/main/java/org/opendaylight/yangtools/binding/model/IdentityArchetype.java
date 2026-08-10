/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import java.util.Comparator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.model.impl.IdentityArchetype0;
import org.opendaylight.yangtools.binding.model.impl.IdentityArchetype1;
import org.opendaylight.yangtools.binding.model.impl.IdentityArchetypeN;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;

/**
 * An {@link Archetype} for a {@link BaseIdentity} specialization generated for an {@link IdentityEffectiveStatement}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface IdentityArchetype extends Archetype, ReturnType
        permits IdentityArchetype0, IdentityArchetype1, IdentityArchetypeN {
    @Override
    IdentityEffectiveStatement statement();

    /**
     * {@return the list of identities this identity extends}
     */
    List<IdentityArchetype> baseIdentities();

    static IdentityArchetype of(final TypeName name, final IdentityEffectiveStatement statement) {
        return new IdentityArchetype0(name, statement);
    }

    static IdentityArchetype of(final TypeName name, final IdentityEffectiveStatement statement,
            final IdentityArchetype baseIdentity) {
        return new IdentityArchetype1(name, statement, baseIdentity);
    }

    static IdentityArchetype of(final TypeName name, final IdentityEffectiveStatement statement,
            final List<IdentityArchetype> baseIdentities) {
        return switch (baseIdentities.size()) {
            case 0 -> of(name, statement);
            case 1 -> of(name, statement, baseIdentities.getFirst());
            default -> {
                final var unique = baseIdentities.stream()
                    .distinct()
                    .sorted(Comparator.comparing(Type::name))
                    .toList();
                yield unique.size() == 1 ?  of(name, statement, unique.getFirst())
                    : new IdentityArchetypeN(name, statement, List.copyOf(unique));
            }
        };
    }
}
