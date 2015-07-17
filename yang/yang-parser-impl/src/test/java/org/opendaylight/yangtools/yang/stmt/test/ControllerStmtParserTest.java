package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.*;

import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;

import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import java.text.ParseException;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.junit.Test;

public class ControllerStmtParserTest {

    @Test
    public void test() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException, ParseException {
        SchemaContext context = StmtTestUtils.parseYangSources("/sal-broker-impl");
        assertNotNull(context);

        Module module = context.findModuleByName("opendaylight-sal-dom-broker-impl", SimpleDateFormatUtil.getRevisionFormat().parse("2013-10-28"));
        assertNotNull(module);

        Set<AugmentationSchema> augmentations = module.getAugmentations();
        boolean checked = false;
        for (AugmentationSchema augmentationSchema : augmentations) {
            DataSchemaNode dataNode = augmentationSchema.getDataChildByName("dom-broker-impl");
            if(dataNode instanceof ChoiceCaseNode) {
                ChoiceCaseNode caseNode = (ChoiceCaseNode) dataNode;
                DataSchemaNode dataNode2 = caseNode.getDataChildByName("async-data-broker");
                if(dataNode2 instanceof ContainerSchemaNode) {
                    ContainerSchemaNode containerNode = (ContainerSchemaNode) dataNode2;
                    DataSchemaNode leaf = containerNode.getDataChildByName("type");
                    List<UnknownSchemaNode> unknownSchemaNodes = leaf.getUnknownSchemaNodes();
                    assertEquals(1, unknownSchemaNodes.size());

                    UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.get(0);
                    assertEquals("dom-async-data-broker", unknownSchemaNode.getQName().getLocalName());
                    assertEquals(unknownSchemaNode.getQName(), unknownSchemaNode.getPath().getLastComponent());

                    //:TODO add test also for path of unknown node in refine

                    checked = true;
                }
            }
        }
        assertTrue(checked);


        Module configModule = context.findModuleByName("config", SimpleDateFormatUtil.getRevisionFormat().parse("2013-04-05"));
        assertNotNull(configModule);

        DataSchemaNode dataNode = configModule.getDataChildByName("modules");
        assertTrue(dataNode instanceof ContainerSchemaNode);

        ContainerSchemaNode moduleContainer = (ContainerSchemaNode) dataNode;
        DataSchemaNode dataChildList = moduleContainer.getDataChildByName("module");

        assertTrue(dataChildList instanceof ListSchemaNode);

        ListSchemaNode listModule = (ListSchemaNode) dataChildList;
        DataSchemaNode dataChildChoice = listModule.getDataChildByName("configuration");

        assertTrue(dataChildChoice instanceof ChoiceSchemaNode);

        ChoiceSchemaNode confChoice = (ChoiceSchemaNode) dataChildChoice;
        ChoiceCaseNode caseNodeByName = confChoice.getCaseNodeByName("dom-broker-impl");

        assertNotNull(caseNodeByName);
        DataSchemaNode dataNode2 = caseNodeByName.getDataChildByName("async-data-broker");
        assertTrue(dataNode2 instanceof ContainerSchemaNode);

        ContainerSchemaNode containerNode = (ContainerSchemaNode) dataNode2;
        DataSchemaNode leaf = containerNode.getDataChildByName("type");
        List<UnknownSchemaNode> unknownSchemaNodes = leaf.getUnknownSchemaNodes();

        assertEquals(1, unknownSchemaNodes.size());

        UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.get(0);

        assertEquals(unknownSchemaNode.getQName(), unknownSchemaNode.getPath().getLastComponent());
        assertEquals("dom-async-data-broker", unknownSchemaNode.getQName().getLocalName());

    }

}
