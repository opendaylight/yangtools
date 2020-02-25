/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;
import java.util.Dictionary;
import java.util.EventListener;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@Beta
@NonNullByDefault
public final class OSGiRegistrations {
    private OSGiRegistrations() {
        // Hidden
    }

    public static <T> Registration register(final BundleContext bundleContext, final Class<T> type, final T service) {
        return register(bundleContext, type, service, null);
    }

    public static <T> Registration register(final BundleContext bundleContext, final Class<T> type, final T service,
            final @Nullable Dictionary<String, ?> properties) {
        final ServiceRegistration<T> reg = bundleContext.registerService(type, service, properties);
        return new AbstractRegistration() {
            @Override
            protected void removeRegistration() {
                reg.unregister();
            }
        };
    }

    public static <T> ObjectRegistration<T> registerObject(final BundleContext bundleContext, final Class<T> type,
            final T service) {
        return registerObject(bundleContext, type, service, null);
    }

    public static <T> ObjectRegistration<T> registerObject(final BundleContext bundleContext,
            final Class<T> type, final T service, final @Nullable Dictionary<String, ?> properties) {
        final ServiceRegistration<T> reg = bundleContext.registerService(type, service, properties);
        return new AbstractObjectRegistration<>(service) {
            @Override
            protected void removeRegistration() {
                reg.unregister();
            }
        };
    }

    public static <T extends EventListener> ListenerRegistration<T> registerListener(final BundleContext bundleContext,
            final Class<T> type, final T service) {
        return registerListener(bundleContext, type, service, null);
    }

    public static <T extends EventListener> ListenerRegistration<T> registerListener(final BundleContext bundleContext,
            final Class<T> type, final T service, final @Nullable Dictionary<String, ?> properties) {
        final ServiceRegistration<T> reg = bundleContext.registerService(type, service, properties);
        return new AbstractListenerRegistration<>(service) {
            @Override
            protected void removeRegistration() {
                reg.unregister();
            }
        };
    }
}
