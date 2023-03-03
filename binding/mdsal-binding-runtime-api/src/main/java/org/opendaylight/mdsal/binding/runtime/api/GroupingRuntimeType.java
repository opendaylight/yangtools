/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;

/**
 * A {@link RuntimeType} associated with a {@code grouping} statement.
 */
public interface GroupingRuntimeType extends CompositeRuntimeType {
    @Override
    GroupingEffectiveStatement statement();

    /**
     * Return the set of all concrete data tree instantiations of this {@code grouping}. This is necessary to completely
     * resolve type information for {@code leafref}s.
     *
     * <p>
     * As an example, consider {@link GroupingRuntimeType} of {@code grouping baz} and it's instantiations roots
     * {@code container one} and {@code container two} define in these three models:
     * <pre>{@code
     *   module baz {
     *     namespace baz;
     *     prefix baz;
     *
     *     grouping baz {
     *       leaf baz {
     *         type leafref {
     *           path "../bar";
     *         }
     *       }
     *     }
     *   }
     *
     *   module one {
     *     namespace one;
     *     prefix one;
     *     import baz { prefix baz; }
     *
     *     container one {
     *       leaf bar {
     *         type string;
     *       }
     *       uses baz:baz;
     *     }
     *   }
     *
     *   module two {
     *     namespace two;
     *     prefix two;
     *     import baz { prefix baz; }
     *
     *     container two {
     *       leaf bar {
     *         type uint16;
     *       }
     *       uses baz:baz;
     *     }
     *   }
     * }</pre>
     *
     * <p>
     * Since these are separate modules, each of them can be part of its own compilation unit and therefore
     * {@code grouping baz} compile-time analysis cannot definitely determine the return type of {@code getBaz()} and
     * must fall back to {@code Object}.
     *
     * <p>
     * At run-time, though, we have a closed world, and therefore we can provide accurate information about
     * instantiation sites: this method will return the {@link CompositeRuntimeType}s for {@code one} and {@code two}.
     * We can then use this information to know that {@code getBaz()} can either be a {@code String} or an
     * {@code Uint32} and which type is appropriate at a particular point in YANG data tree.
     *
     * @return The set instantiated {@link CompositeRuntimeType}s which use this grouping
     */
    default @NonNull List<CompositeRuntimeType> instantiations() {
        final var users = directUsers();
        return switch (users.size()) {
            case 0 -> List.of();
            case 1 -> {
                final var user = users.get(0);
                yield user instanceof GroupingRuntimeType grouping ? grouping.instantiations() : List.of(user);
            }
            default -> users.stream()
                .flatMap(user -> user instanceof GroupingRuntimeType grouping ? grouping.instantiations().stream()
                    : Stream.of(user))
                .distinct()
                .collect(Collectors.toUnmodifiableList());
        };
    }

    /**
     * Support method for {@link #instantiations()}. This method's return, unlike {@link #instantiations()} can contain
     * other {@link GroupingRuntimeType}s.
     *
     * @return Direct users of this grouping
     */
    @NonNull List<CompositeRuntimeType> directUsers();
}
