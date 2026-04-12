/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
final class Uint32Support extends AbstractCanonicalValueSupport<Uint32> {
    static final CanonicalValueSupport<Uint32> INSTANCE = new Uint32Support();

    private Uint32Support() {
        super(Uint32.class);
    }

    @Override
    public ValidationResult<Uint32> fromString(final String str) {
        try {
            return new ValidatedValue<>(Uint32.valueOf(str));
        } catch (IllegalArgumentException e) {
            return CanonicalValueViolation.of(e);
        }
    }
}
