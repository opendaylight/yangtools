package org.opendaylight.yangtools.it.yang.runtime.tests;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import javassist.ClassPool;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.extension.yang.ext.rev130709.RpcContextRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.test.regression.augmentation.base.rev140424.Choices;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.test.regression.augmentation.base.rev140424.grouping.GroupingDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.test.regression.augmentation.ext.rev140424.choices.augmentable.choice.ext.with.grouping.augmentations.ExtWithGroupingAugmentations;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.test.regression.augmentation.ext.rev140424.choices.augmentable.choice.ext.with.grouping.augmentations.ExtWithGroupingAugmentationsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.test.regression.augmentation.ext.rev140424.choices.augmentable.choice.ext.with.grouping.augmentations.ext.with.grouping.augmentations.InUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.test.regression.augmentation.ext.rev140424.choices.augmentable.choice.ext.with.grouping.augmentations.ext.with.grouping.augmentations.InUsesAugmentBuilder;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.impl.RuntimeGeneratedMappingServiceImpl;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import com.google.common.base.Optional;

public class RuntimeCodecAugmentationWithGroupingsAndCasesTest {

    private static final InstanceIdentifier<ExtWithGroupingAugmentations> GROUPING_AUGMENTATIONS_PATH = InstanceIdentifier
            .builder(Choices.class).child(ExtWithGroupingAugmentations.class).build();

    private ModuleInfoBackedContext moduleInfoContext;

    private Optional<SchemaContext> schemaContext;

    private RuntimeGeneratedMappingServiceImpl mappingService;


    @Before
    public void setUp() throws Exception {
        YangModuleInfo yangExtInfo = BindingReflections.getModuleInfo(RpcContextRef.class);
        YangModuleInfo baseModuleInfo = BindingReflections.getModuleInfo(Choices.class);
        YangModuleInfo augmentationModuleInfo = BindingReflections.getModuleInfo(org.opendaylight.yang.gen.v1.urn.opendaylight.yang.test.regression.augmentation.ext.rev140424.choices.augmentable.choice.ext.with.grouping.augmentations.ExtWithGroupingAugmentations.class);

        moduleInfoContext = ModuleInfoBackedContext.create();
        moduleInfoContext.registerModuleInfo(yangExtInfo);
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
    public void testSerialization() {

        ExtWithGroupingAugmentations caseData = new ExtWithGroupingAugmentationsBuilder() //
                .setGroupingData(new GroupingDataBuilder() //
                        .addAugmentation(InUsesAugment.class, new InUsesAugmentBuilder() //
                                .setExtAumentation("InUses") //
                                .build()) //
                        .build()) //
                .build();

        Entry<org.opendaylight.yangtools.yang.data.api.InstanceIdentifier, CompositeNode> result = mappingService
                .toDataDom(new SimpleEntry(GROUPING_AUGMENTATIONS_PATH, caseData));
        assertNotNull(result);
    }
}
