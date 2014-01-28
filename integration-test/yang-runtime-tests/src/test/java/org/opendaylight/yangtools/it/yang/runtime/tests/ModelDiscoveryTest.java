/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.it.yang.runtime.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

public class ModelDiscoveryTest {

    public static final YangModuleInfo TOPOLOGY_OLD_MODULE = org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev130712.$YangModuleInfoImpl
            .getInstance();
    public static final YangModuleInfo TOPOLOGY_NEW_MODULE = org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.$YangModuleInfoImpl
            .getInstance();

    public static final String METAINF_PATH = "META-INF/services/" + YangModelBindingProvider.class.getName();

    @Test
    public void discoveryUsing_BindingReflections_TCCL() {

        ImmutableSet<YangModuleInfo> moduleInfoSet = BindingReflections.loadModuleInfos();
        assertNotNull(moduleInfoSet);
        assertFalse(moduleInfoSet.isEmpty());
        assertTrue(moduleInfoSet.contains(TOPOLOGY_NEW_MODULE));
    }

    @Test
    public void discoveryUsing_BindingReflections_classloader_partialServiceMetadata() throws Exception {

        ClassLoader topologyModelClassLoader = new ClassLoader(Thread.currentThread().getContextClassLoader()) {

            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                if (METAINF_PATH.equals(name)) {
                    Vector<URL> topologyUrlVector = new Vector<>();
                    topologyUrlVector.add(TOPOLOGY_NEW_MODULE.getClass().getResource("/" + METAINF_PATH));
                    return topologyUrlVector.elements();
                }
                return super.getResources(name);
            }
        };

        ImmutableSet<YangModuleInfo> moduleInfoSet = BindingReflections.loadModuleInfos(topologyModelClassLoader);
        assertNotNull(moduleInfoSet);
        assertFalse(moduleInfoSet.isEmpty());
        assertTrue(moduleInfoSet.contains(TOPOLOGY_NEW_MODULE));
    }

    @Test
    public void moduleInfoBackedContextTCCL() throws Exception {

        ModuleInfoBackedContext context = ModuleInfoBackedContext.create(GeneratedClassLoadingStrategy.getAlwaysFailClassLoadingStrategy());

        ImmutableSet<YangModuleInfo> moduleInfoSet = BindingReflections.loadModuleInfos();

        context.addModuleInfos(moduleInfoSet);
        assertNotNull(moduleInfoSet);
        assertFalse(moduleInfoSet.isEmpty());
        assertTrue(moduleInfoSet.contains(TOPOLOGY_NEW_MODULE));

        Class<?> linkClass = context.loadClass(Link.class.getName());
        assertNotNull(linkClass);
        assertEquals(Link.class, linkClass);
        Optional<SchemaContext> schemaContext = context.tryToCreateSchemaContext();
        assertNotNull(schemaContext);
        assertNotNull(schemaContext.get());
    }

}
