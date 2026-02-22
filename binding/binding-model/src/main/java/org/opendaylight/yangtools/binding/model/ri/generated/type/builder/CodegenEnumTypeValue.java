/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.Status;

final class CodegenEnumTypeValue extends AbstractEnumTypeValue {
    private final @NonNull Status status;
    private final String description;
    private final String reference;

    CodegenEnumTypeValue(final int value, final String name, final String constantName, final Status status,
            final String description, final String reference) {
        super(value, name, constantName);
        this.status = requireNonNull(status);
        this.description = description;
        this.reference = reference;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }

    @Override
    public Status getStatus() {
        return status;
    }
}