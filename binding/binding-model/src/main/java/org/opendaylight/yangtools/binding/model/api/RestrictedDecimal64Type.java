/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link RestrictedType} corresponding to {@link Decimal64Type}.
 */
@NonNullByDefault
public sealed interface RestrictedDecimal64Type extends Decimal64Type, RestrictedType
        permits DefaultRestrictedDecimal64Type {
    @Override
    Decimal64Type withoutRestrictions();
}
