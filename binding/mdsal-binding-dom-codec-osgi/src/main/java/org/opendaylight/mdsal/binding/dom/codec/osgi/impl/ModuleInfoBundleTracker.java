/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.osgi.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks bundles and attempts to retrieve YangModuleInfo, which is then fed into ModuleInfoRegistry.
 */
final class ModuleInfoBundleTracker implements BundleTrackerCustomizer<Collection<ObjectRegistration<YangModuleInfo>>> {
    private static final Logger LOG = LoggerFactory.getLogger(ModuleInfoBundleTracker.class);
    // FIXME: this should be in a place shared with maven-sal-api-gen-plugin
    private static final String MODULE_INFO_PROVIDER_PATH_PREFIX = "META-INF/services/";

    private static final String YANG_MODLE_BINDING_PROVIDER_SERVICE = MODULE_INFO_PROVIDER_PATH_PREFIX
            + YangModelBindingProvider.class.getName();

    private static final Pattern BRACE_PATTERN = Pattern.compile("{}", Pattern.LITERAL);

    private final OsgiModuleInfoRegistry moduleInfoRegistry;

    private volatile boolean starting = true;

    ModuleInfoBundleTracker(final OsgiModuleInfoRegistry moduleInfoRegistry) {
        this.moduleInfoRegistry = requireNonNull(moduleInfoRegistry);
    }

    void finishStart() {
        starting = false;
        moduleInfoRegistry.updateService();
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public Collection<ObjectRegistration<YangModuleInfo>> addingBundle(final Bundle bundle, final BundleEvent event) {
        final URL resource = bundle.getEntry(YANG_MODLE_BINDING_PROVIDER_SERVICE);
        if (resource == null) {
            LOG.debug("Bundle {} does not have an entry for {}", bundle, YANG_MODLE_BINDING_PROVIDER_SERVICE);
            return ImmutableList.of();
        }

        LOG.debug("Got addingBundle({}) with YangModelBindingProvider resource {}", bundle, resource);
        final List<String> lines;
        try {
            lines = Resources.readLines(resource, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Error while reading {} from bundle {}", resource, bundle, e);
            return ImmutableList.of();
        }

        if (lines.isEmpty()) {
            LOG.debug("Bundle {} has empty services for {}", bundle, YANG_MODLE_BINDING_PROVIDER_SERVICE);
            return ImmutableList.of();
        }

        final List<ObjectRegistration<YangModuleInfo>> registrations = new ArrayList<>(lines.size());
        for (String moduleInfoName : lines) {
            LOG.trace("Retrieve ModuleInfo({}, {})", moduleInfoName, bundle);
            final YangModuleInfo moduleInfo;
            try {
                moduleInfo = retrieveModuleInfo(moduleInfoName, bundle);
            } catch (RuntimeException e) {
                LOG.warn("Failed to acquire {} from bundle {}, ignoring it", moduleInfoName, bundle, e);
                continue;
            }

            registrations.add(moduleInfoRegistry.registerModuleInfo(moduleInfo));
        }

        if (!starting) {
            moduleInfoRegistry.updateService();
        }

        LOG.trace("Bundle {} resultend in registrations {}", bundle, registrations);
        return registrations;
    }

    @Override
    public void modifiedBundle(final Bundle bundle, final BundleEvent event,
            final Collection<ObjectRegistration<YangModuleInfo>> object) {
        // No-op
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public void removedBundle(final Bundle bundle, final BundleEvent event,
            final Collection<ObjectRegistration<YangModuleInfo>> regs) {
        if (regs == null) {
            return;
        }

        for (ObjectRegistration<YangModuleInfo> reg : regs) {
            try {
                reg.close();
            } catch (Exception e) {
                LOG.warn("Unable to unregister YangModuleInfo {}", reg.getInstance(), e);
            }
        }
    }

    private static YangModuleInfo retrieveModuleInfo(final String moduleInfoClass, final Bundle bundle) {
        final Class<?> clazz = loadClass(moduleInfoClass, bundle);
        if (!YangModelBindingProvider.class.isAssignableFrom(clazz)) {
            String errorMessage = logMessage("Class {} does not implement {} in bundle {}", clazz,
                YangModelBindingProvider.class, bundle);
            throw new IllegalStateException(errorMessage);
        }

        final YangModelBindingProvider instance;
        try {
            Object instanceObj = clazz.newInstance();
            instance = YangModelBindingProvider.class.cast(instanceObj);
        } catch (InstantiationException e) {
            String errorMessage = logMessage("Could not instantiate {} in bundle {}, reason {}", moduleInfoClass,
                bundle, e);
            throw new IllegalStateException(errorMessage, e);
        } catch (IllegalAccessException e) {
            String errorMessage = logMessage("Illegal access during instantiation of class {} in bundle {}, reason {}",
                    moduleInfoClass, bundle, e);
            throw new IllegalStateException(errorMessage, e);
        }

        try {
            return instance.getModuleInfo();
        } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
            throw new IllegalStateException("Error while executing getModuleInfo on " + instance, e);
        }
    }

    private static Class<?> loadClass(final String moduleInfoClass, final Bundle bundle) {
        try {
            return bundle.loadClass(moduleInfoClass);
        } catch (ClassNotFoundException e) {
            String errorMessage = logMessage("Could not find class {} in bundle {}, reason {}", moduleInfoClass, bundle,
                e);
            throw new IllegalStateException(errorMessage);
        }
    }

    @SuppressFBWarnings("SLF4J_UNKNOWN_ARRAY")
    private static String logMessage(final String slfMessage, final Object... params) {
        LOG.info(slfMessage, params);
        return String.format(BRACE_PATTERN.matcher(slfMessage).replaceAll("%s"), params);
    }
}
