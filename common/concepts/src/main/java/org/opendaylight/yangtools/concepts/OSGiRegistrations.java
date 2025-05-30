/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * A {@link Registration} projection of similar OSGi concepts, most notably {@link ServiceRegistration}.
 */
// Note: not 'OSGiServiceRegistration' for shorter names. We really would like to claim the 'Service' part
//       of the namespace, but that would make us OSGi-specific and would be ugly to use anyway.
@NonNullByDefault
public final class OSGiRegistrations {
    private OSGiRegistrations() {
        // Hidden on purpose
    }

    public static BaseRegistration of(final ServiceRegistration<?> serviceReg) {
        return new OSGiServiceRegistration(serviceReg);
    }

    public static <S> BaseRegistration of(final BundleContext ctx, final Class<S> clazz, final S service) {
        return of(ctx, clazz, service, null);
    }

    public static <S> BaseRegistration of(final BundleContext ctx, final Class<S> clazz, final S service,
            final @Nullable Map<String, ?> properties) {
        return of(ctx.registerService(clazz, service,
            properties == null ? null : FrameworkUtil.asDictionary(properties)));
    }
}
