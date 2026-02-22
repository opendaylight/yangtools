/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.Status;

final class RuntimeEnumTypeValue extends AbstractEnumTypeValue {
    @NonNullByDefault
    RuntimeEnumTypeValue(final int value, final String name, final String constantName) {
        super(value, name, constantName);
    }

    @Override
    public Optional<String> getDescription() {
        throw uoe();
    }

    @Override
    public Optional<String> getReference() {
        throw uoe();
    }

    @Override
    public Status getStatus() {
        throw uoe();
    }

    private static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException("Not available at runtime");
    }
}