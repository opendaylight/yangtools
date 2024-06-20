/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public final class OSGiModelRuntime {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiModelRuntime.class);

    private final YangModuleInfoRegistry moduleRegistry;
    private final YangModuleInfoScanner bundleTracker;

    @Activate
    public OSGiModelRuntime(@Reference final YangParserFactory parserFactory,
            @Reference(target = "(component.factory=" + OSGiModuleInfoSnapshotImpl.FACTORY_NAME + ")")
            final ComponentFactory<OSGiModuleInfoSnapshotImpl> contextFactory,
            final BundleContext ctx) {
        LOG.info("Model Runtime starting");
        moduleRegistry = YangModuleInfoRegistry.create(ctx, contextFactory, parserFactory);
        bundleTracker = new YangModuleInfoScanner(ctx, moduleRegistry);
        bundleTracker.open();
        moduleRegistry.enableScannerAndUpdate();
        LOG.info("Model Runtime started");
    }

    @Deactivate
    void deactivate() {
        LOG.info("Model Runtime stopping");
        moduleRegistry.close();
        bundleTracker.close();
        LOG.info("Model Runtime stopped");
    }
}
