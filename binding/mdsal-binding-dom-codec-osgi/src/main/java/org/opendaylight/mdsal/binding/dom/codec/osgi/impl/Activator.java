/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.osgi.impl;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.binding.dom.codec.osgi.BindingRuntimeContextService;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Activator implements BundleActivator {
    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private final List<ServiceRegistration<?>> registrations = new ArrayList<>(2);

    private ModuleInfoBundleTracker bundleTracker = null;
    private SimpleBindingRuntimeContextService service = null;

    @Override
    public void start(final BundleContext context) {
        LOG.info("Binding-DOM codec starting");

        // XXX: this will use thread-context class loader, which is probably appropriate
        final ModuleInfoBackedContext moduleInfoBackedContext = ModuleInfoBackedContext.create();

        service = new SimpleBindingRuntimeContextService(context, moduleInfoBackedContext, moduleInfoBackedContext);

        final OsgiModuleInfoRegistry registry = new OsgiModuleInfoRegistry(moduleInfoBackedContext,
                moduleInfoBackedContext, service);

        LOG.debug("Starting Binding-DOM codec bundle tracker");
        bundleTracker = new ModuleInfoBundleTracker(context, registry);
        bundleTracker.open();

        LOG.debug("Starting Binding-DOM runtime context service");
        service.open();

        LOG.debug("Registering Binding-DOM codec services");
        registrations.add(context.registerService(BindingRuntimeContextService.class, service, null));
        registrations.add(context.registerService(ClassLoadingStrategy.class, moduleInfoBackedContext, null));

        LOG.info("Binding-DOM codec started");
    }

    @Override
    public void stop(final BundleContext context) {
        LOG.info("Binding-DOM codec stopping");

        LOG.debug("Unregistering Binding-DOM codec services");
        registrations.forEach(ServiceRegistration::unregister);
        registrations.clear();

        LOG.debug("Stopping Binding-DOM codec bundle tracker");
        bundleTracker.close();

        LOG.debug("Stoping Binding-DOM runtime context service");
        service.close();

        LOG.info("Binding-DOM codec stopped");
    }
}
