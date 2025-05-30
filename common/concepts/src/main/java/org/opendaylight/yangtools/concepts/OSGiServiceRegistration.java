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
 * A {@link Registration} wrapping a {@link OSGiServiceRegistration}.
 */
@NonNullByDefault
final class OSGiServiceRegistration extends GenericRegistration<org.osgi.framework.ServiceRegistration<?>> {
    OSGiServiceRegistration(final org.osgi.framework.ServiceRegistration<?> serviceReg) {
        super(serviceReg);
    }

    @Override
    protected void clean(final org.osgi.framework.ServiceRegistration<?> serviceReg) {
        serviceReg.unregister();
    }

    @Override
    protected String resourceName() {
        return "registration";
    }
}
