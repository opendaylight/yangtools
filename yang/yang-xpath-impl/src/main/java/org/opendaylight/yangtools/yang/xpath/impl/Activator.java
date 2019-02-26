/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * YANG XPath implementation activator. Publishes a {@link YangXPathParserFactory} implementation on bundle start.
 *
 * @author Robert Varga
 */
public final class Activator implements BundleActivator {
    private @Nullable ServiceRegistration<YangXPathParserFactory> registration;

    @Override
    public void start(final @Nullable BundleContext context) throws Exception {
        registration = context.registerService(YangXPathParserFactory.class, new AntlrXPathParserFactory(), null);
    }

    @Override
    public void stop(final @Nullable BundleContext context) throws Exception {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }
}
