/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link BaseRegistration} which defers to another {@link AutoCloseable} resource.
 */
@NonNullByDefault
final class ResourceRegistration extends GenericRegistration<AutoCloseable> {
    ResourceRegistration(final AutoCloseable resource) {
        super(resource);
    }

    @Override
    protected void clean(final AutoCloseable resource) throws Exception {
        resource.close();
    }

    @Override
    protected String resourceName() {
        return "resource";
    }
}