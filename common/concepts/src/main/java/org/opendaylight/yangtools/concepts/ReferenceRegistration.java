/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.lang.ref.Reference;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link Registration} of a {@link Reference}.
 */
@NonNullByDefault
final class ReferenceRegistration extends GenericRegistration<Reference<?>> {
    ReferenceRegistration(final Reference<?> reference) {
        super(reference);
    }

    @Override
    protected void clean(final Reference<?> reference) {
        reference.clear();
    }

    @Override
    protected String resourceName() {
        return "reference";
    }
}