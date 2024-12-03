/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public record UnionCharacterGroup(List<CharacterGroup> components) implements CharacterGroup {
    public UnionCharacterGroup {
        components = List.copyOf(components);
        if (components.size() < 2) {
            throw new IllegalArgumentException("Require at least two components");
        }
    }

    @Override
    public void appendPatternFragment(final StringBuilder sb) {
        for (var component : components) {
            component.appendPatternFragment(sb);
        }
    }
}