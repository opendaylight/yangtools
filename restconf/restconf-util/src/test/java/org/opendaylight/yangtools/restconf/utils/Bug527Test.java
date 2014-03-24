/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.utils;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

import java.io.InputStream;

import javassist.ClassPool;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.Alpha;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.alpha.Beta;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.group.a.xcont.Xchoice;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.group.b.xcont.xchoice.Xcase;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev140321.Beta1;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.impl.RuntimeGeneratedMappingServiceImpl;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public class Bug527Test {

    private RuntimeGeneratedMappingServiceImpl mappingService;

    @Before
    public void setup() {
        this.mappingService = new RuntimeGeneratedMappingServiceImpl();
        this.mappingService.setPool(new ClassPool());
        this.mappingService.init();

        final ModuleInfoBackedContext moduleInfo = ModuleInfoBackedContext.create();
        moduleInfo.addModuleInfos(BindingReflections.loadModuleInfos());
        this.mappingService.onGlobalContextUpdated(moduleInfo.tryToCreateSchemaContext().get());
    }

    @Test
    public void testToDataObjectMappingWithNestedAugmentations() {
        InstanceIdentifier<Alpha> alphaId = InstanceIdentifier.builder(Alpha.class).toInstance();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("topology-bug527.xml");
        DataSchemaNode dataSchema = RestconfUtils.toRestconfIdentifier(alphaId, mappingService,
                mappingService.getSchemaContext()).getValue();
        Alpha alpha = (Alpha) RestconfUtils.dataObjectFromInputStream(alphaId, is, mappingService.getSchemaContext(),
                mappingService, dataSchema);
        assertNotNull(alpha);

        Beta beta = alpha.getBeta();
        Beta1 beta1 = beta.getAugmentation(Beta1.class);

        Xchoice xchoice = beta1.getXcont().getXchoice();
        assertNotNull(xchoice);

        Xcase acase = (Xcase) xchoice;
        assertEquals("idx2", acase.getIdx());
    }

}
