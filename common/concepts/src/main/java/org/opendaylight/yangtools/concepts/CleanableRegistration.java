/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.lang.ref.Cleaner.Cleanable;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link Registration} wrapping a {@link Cleanable}.
 */
@NonNullByDefault
final class CleanableRegistration extends GenericRegistration<Cleanable> {
    CleanableRegistration(final Cleanable cleanable) {
        super(cleanable);
    }

    @Override
    protected void clean(final Cleanable cleanable) {
        cleanable.clean();
    }

    @Override
    protected String resourceName() {
        return "cleanable";
    }
}