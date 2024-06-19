/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.GroupingRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;

public final class DefaultGroupingRuntimeType extends AbstractCompositeRuntimeType<GroupingEffectiveStatement>
        implements GroupingRuntimeType {
    private final @Nullable Object directUsers;

    public DefaultGroupingRuntimeType(final GeneratedType bindingType, final GroupingEffectiveStatement statement,
            final List<RuntimeType> children, final List<? extends CompositeRuntimeType> directUsers) {
        super(bindingType, statement, children);
        this.directUsers = switch (directUsers.size()) {
            case 0 -> null;
            case 1 -> Objects.requireNonNull(directUsers.get(0));
            default -> directUsers.stream().map(Objects::requireNonNull).toArray(CompositeRuntimeType[]::new);
        };
    }

    @Override
    public List<CompositeRuntimeType> directUsers() {
        final var local = directUsers;
        if (local == null) {
            return List.of();
        } else if (local instanceof CompositeRuntimeType[] array) {
            return Collections.unmodifiableList(Arrays.asList(array));
        } else {
            return List.of((CompositeRuntimeType) local);
        }
    }
}
