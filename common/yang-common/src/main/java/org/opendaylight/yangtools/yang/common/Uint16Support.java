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
final class Uint16Support extends AbstractCanonicalValueSupport<Uint16> {
    static final CanonicalValueSupport<Uint16> INSTANCE = new Uint16Support();

    private Uint16Support() {
        super(Uint16.class);
    }

    @Override
    public ValidationResult<Uint16> fromString(final String str) {
        try {
            return new ValidatedValue<>(Uint16.valueOf(str));
        } catch (IllegalArgumentException e) {
            return CanonicalValueViolation.of(e);
        }
    }
}
