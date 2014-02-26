package org.opendaylight.yangtools.it.yang.runtime.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import javassist.ClassPool;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.test.regression.bug466.augmentation.empty.rev140226.Bug4661;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.test.regression.bug466.base.rev140226.Bug466;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.impl.RuntimeGeneratedMappingServiceImpl;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.impl.codec.DataContainerCodec;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import com.google.common.base.Optional;

public class Bug466EmptyAugmentationCodecs {



    private YangModuleInfo baseModuleInfo;
    private YangModuleInfo augmentationModuleInfo;
    private ModuleInfoBackedContext moduleInfoContext;
    private RuntimeGeneratedMappingServiceImpl mappingService;
    private Optional<SchemaContext> schemaContext;


    @Before
    public void setup()throws Exception {
        baseModuleInfo = BindingReflections.getModuleInfo(Bug466.class);
        augmentationModuleInfo = BindingReflections.getModuleInfo(Bug4661.class);

        moduleInfoContext = ModuleInfoBackedContext.create();
        moduleInfoContext.registerModuleInfo(baseModuleInfo);
        moduleInfoContext.registerModuleInfo(augmentationModuleInfo);
        schemaContext = moduleInfoContext.tryToCreateSchemaContext();
        assertNotNull(schemaContext);
        assertTrue(schemaContext.isPresent());

        mappingService = new RuntimeGeneratedMappingServiceImpl(moduleInfoContext);
        mappingService.setPool(ClassPool.getDefault());
        mappingService.init();
        mappingService.onGlobalContextUpdated(schemaContext.get());

    }


    @Test
    public void proactiveGenerationOfAugmentationCodecs() {
        DataContainerCodec<Bug466> codec = mappingService.getCodecRegistry().getCodecForDataObject(Bug466.class);
        assertNotNull(codec);
    }

}
