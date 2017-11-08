/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.osgi;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * YANG parser implementation activator. Publishes a {@link YangParserFactory} implementation on bundle start.
 *
 * @author Robert Varga
 */
public final class Activator implements BundleActivator {
    private ServiceRegistration<@NonNull YangParserFactory> registration;

    @Override
    public void start(final BundleContext context) throws Exception {
        registration = context.registerService(YangParserFactory.class, new YangParserFactoryImpl(), null);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }
}
