/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.osgi.impl;

import org.opendaylight.binding.runtime.api.ClassLoadingStrategy;
import org.opendaylight.binding.runtime.spi.GeneratedClassLoadingStrategy;
import org.opendaylight.binding.runtime.spi.ModuleInfoBackedContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public final class BindingClassLoadingStrategy implements ClassLoadingStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(BindingClassLoadingStrategy.class);

    @Reference
    YangParserFactory factory = null;

    private ModuleInfoBundleTracker bundleTracker = null;
    private ModuleInfoBackedContext moduleInfoBackedContext = null;

    @Activate
    void activate(final BundleContext ctx) {
        LOG.info("Binding-DOM codec starting");

        moduleInfoBackedContext = ModuleInfoBackedContext.create("binding-dom-codec", factory,
            // FIXME: This is the fallback strategy, it should not be needed
            GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy());

        final OsgiModuleInfoRegistry registry = new OsgiModuleInfoRegistry(moduleInfoBackedContext,
                moduleInfoBackedContext);

        LOG.debug("Starting Binding-DOM codec bundle tracker");
        bundleTracker = new ModuleInfoBundleTracker(ctx, registry);
        bundleTracker.open();

        LOG.info("Binding-DOM codec started");
    }

    @Deactivate
    void deactivate() {
        LOG.info("Binding-DOM codec stopping");
        LOG.debug("Stopping Binding-DOM codec bundle tracker");
        bundleTracker.close();
        moduleInfoBackedContext = null;
        bundleTracker = null;
        LOG.info("Binding-DOM codec stopped");
    }

    @Override
    public Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
        return moduleInfoBackedContext.loadClass(fullyQualifiedName);
    }
}
