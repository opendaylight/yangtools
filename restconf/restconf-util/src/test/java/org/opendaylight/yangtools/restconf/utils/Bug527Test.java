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
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.Factory;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.factory.Alpha;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.factory.Beta;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.machine.ext.types.machine.type.TypeA;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.machine.types.Types;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.machine.types.types.MachineType;
import org.opendaylight.yang.gen.v1.urn.yang.baz.rev140321.machine.def.Machine;
import org.opendaylight.yang.gen.v1.urn.yang.baz.rev140321.machine.def.machine.Atts;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev140321.Atts1;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev140321.Atts2;
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
        InstanceIdentifier<Factory> factoryId = InstanceIdentifier.builder(Factory.class).toInstance();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("topology-bug527.xml");
        DataSchemaNode dataSchema = RestconfUtils.toRestconfIdentifier(factoryId, mappingService,
                mappingService.getSchemaContext()).getValue();

        Factory factory = (Factory) RestconfUtils.dataObjectFromInputStream(factoryId, is,
                mappingService.getSchemaContext(), mappingService, dataSchema);
        assertNotNull(factory);

        Alpha alpha = factory.getAlpha();
        Machine alphaMachine = alpha.getMachine();
        Atts alphaAtts = alphaMachine.getAtts();
        Atts1 alphaAttsAug = alphaAtts.getAugmentation(Atts1.class);
        Types alphaMachineTypes = alphaAttsAug.getTypes();
        MachineType alphaMachineType = alphaMachineTypes.getMachineType();
        TypeA alphaTypeA = (TypeA) alphaMachineType;
        assertEquals("id-alpha", alphaTypeA.getId());

        Beta beta = factory.getBeta();
        Machine betaMachine = beta.getMachine();
        Atts betaAtts = betaMachine.getAtts();
        Atts2 betaAttsAug = betaAtts.getAugmentation(Atts2.class);
        Types betaMachineTypes = betaAttsAug.getTypes();
        MachineType betaMachineType = betaMachineTypes.getMachineType();
        TypeA betaTypeA = (TypeA) betaMachineType;
        assertEquals("id-beta", betaTypeA.getId());
    }

}
