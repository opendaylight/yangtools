/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;

import javassist.ClassPool;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.CreateFactoryInput;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.Factory;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.factory.Alpha;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.factory.Beta;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.machine.ext.types.machine.type.TypeA;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.machine.types.Types;
import org.opendaylight.yang.gen.v1.urn.yang.bar.rev140321.machine.types.types.MachineType;
import org.opendaylight.yang.gen.v1.urn.yang.baz.rev140321.machine.def.Machine;
import org.opendaylight.yang.gen.v1.urn.yang.baz.rev140321.machine.def.machine.Atts;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev140321.AlphaExt;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev140321.BetaExt;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev140321.CreateExt;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.impl.RuntimeGeneratedMappingServiceImpl;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.impl.CompositeNodeTOImpl;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public class Bug527Test {

    private RuntimeGeneratedMappingServiceImpl mappingService;

    @Before
    public void setup() {
        this.mappingService = new RuntimeGeneratedMappingServiceImpl(new ClassPool());

        final ModuleInfoBackedContext moduleInfo = ModuleInfoBackedContext.create();
        moduleInfo.addModuleInfos(BindingReflections.loadModuleInfos());
        this.mappingService.onGlobalContextUpdated(moduleInfo.tryToCreateSchemaContext().get());
    }

    @Test
    public void testToDataObjectMappingWithNestedAugmentations() {
        InstanceIdentifier<Factory> factoryId = InstanceIdentifier.builder(Factory.class).build();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("topology-bug527.xml");
        DataSchemaNode dataSchema = RestconfUtils.toRestconfIdentifier(factoryId, mappingService,
                mappingService.getSchemaContext()).getValue();

        Factory factory = (Factory) RestconfUtils.dataObjectFromInputStream(factoryId, is,
                mappingService.getSchemaContext(), mappingService, dataSchema);
        assertNotNull(factory);

        Alpha alpha = factory.getAlpha();
        Machine alphaMachine = alpha.getMachine();
        Atts alphaAtts = alphaMachine.getAtts();
        AlphaExt alphaAttsAug = alphaAtts.getAugmentation(AlphaExt.class);
        Types alphaMachineTypes = alphaAttsAug.getTypes();
        MachineType alphaMachineType = alphaMachineTypes.getMachineType();
        TypeA alphaTypeA = (TypeA) alphaMachineType;
        assertEquals("id-alpha", alphaTypeA.getId());
        assertNull(alphaAtts.getAugmentation(BetaExt.class));

        Beta beta = factory.getBeta();
        Machine betaMachine = beta.getMachine();
        Atts betaAtts = betaMachine.getAtts();
        BetaExt betaAttsAug = betaAtts.getAugmentation(BetaExt.class);
        Types betaMachineTypes = betaAttsAug.getTypes();
        MachineType betaMachineType = betaMachineTypes.getMachineType();
        TypeA betaTypeA = (TypeA) betaMachineType;
        assertEquals("id-beta", betaTypeA.getId());
        assertNull(betaAtts.getAugmentation(AlphaExt.class));

        InstanceIdentifier<? extends DataObject> alphaPath = InstanceIdentifier.builder(Factory.class).child(Alpha.class).build();
        CompositeNode domAlpha = mappingService.toDataDom(new SimpleEntry<InstanceIdentifier<? extends DataObject>,DataObject>(alphaPath,alpha)).getValue();
        assertNotNull(domAlpha);

        CompositeNodeTOImpl domInput = new CompositeNodeTOImpl(QName.create(domAlpha.getNodeType(),"input"),null, domAlpha.getValue());

        CreateFactoryInput nestedInput = (CreateFactoryInput) mappingService.dataObjectFromDataDom(CreateFactoryInput.class, domInput);
        assertNotNull(nestedInput);
        assertNotNull(nestedInput.getMachine().getAtts().getAugmentation(CreateExt.class));


    }

}
