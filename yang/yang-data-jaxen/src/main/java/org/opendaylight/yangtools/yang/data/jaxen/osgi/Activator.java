/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen.osgi;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.xpath.XPathSchemaContextFactory;
import org.opendaylight.yangtools.yang.data.jaxen.JaxenSchemaContextFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * YANG Jaxen XPath implementation activator. Publishes a {@link XPathSchemaContextFactory} implementation on bundle
 * start.
 *
 * @author Robert Varga
 */
public final class Activator implements BundleActivator {
    private ServiceRegistration<@NonNull XPathSchemaContextFactory> registration;

    @Override
    public void start(final BundleContext context) throws Exception {
        registration = context.registerService(XPathSchemaContextFactory.class, new JaxenSchemaContextFactory(), null);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }
}
