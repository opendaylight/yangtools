/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.it.yang.runtime.tests;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;

import com.google.common.collect.ImmutableSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

}
