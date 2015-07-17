package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.*;

import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;

import org.opendaylight.yangtools.yang.model.api.UsesNode;
import java.net.URI;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.QName;
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
    public void test() throws SourceException, FileNotFoundException,
            ReactorException, URISyntaxException, ParseException {
        SchemaContext context = StmtTestUtils
                .parseYangSources("/sal-broker-impl");
        assertNotNull(context);

        salDomBrokerImplModuleTest(context);
        configModuleTest(context);
    }

    private void salDomBrokerImplModuleTest(SchemaContext context)
            throws ParseException {
        Module module = context.findModuleByName(
                "opendaylight-sal-dom-broker-impl", SimpleDateFormatUtil
                        .getRevisionFormat().parse("2013-10-28"));
        assertNotNull(module);

        Set<AugmentationSchema> augmentations = module.getAugmentations();
        boolean checked = false;
        for (AugmentationSchema augmentationSchema : augmentations) {
            DataSchemaNode dataNode = augmentationSchema
                    .getDataChildByName("dom-broker-impl");
            if (dataNode instanceof ChoiceCaseNode) {
                ChoiceCaseNode caseNode = (ChoiceCaseNode) dataNode;
                DataSchemaNode dataNode2 = caseNode
                        .getDataChildByName("async-data-broker");
                if (dataNode2 instanceof ContainerSchemaNode) {
                    ContainerSchemaNode containerNode = (ContainerSchemaNode) dataNode2;
                    DataSchemaNode leaf = containerNode
                            .getDataChildByName("type");
                    List<UnknownSchemaNode> unknownSchemaNodes = leaf
                            .getUnknownSchemaNodes();
                    assertEquals(1, unknownSchemaNodes.size());

                    UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes
                            .get(0);
                    assertEquals("dom-async-data-broker", unknownSchemaNode
                            .getQName().getLocalName());
                    assertEquals(unknownSchemaNode.getQName(),
                            unknownSchemaNode.getPath().getLastComponent());

                    // :TODO add test also for path of unknown node in refine

                    checked = true;
                }
            }
        }
        assertTrue(checked);
    }

    private void configModuleTest(SchemaContext context) throws ParseException,
            URISyntaxException {
        Module configModule = context.findModuleByName("config",
                SimpleDateFormatUtil.getRevisionFormat().parse("2013-04-05"));
        assertNotNull(configModule);

        DataSchemaNode dataNode = configModule.getDataChildByName("modules");
        assertTrue(dataNode instanceof ContainerSchemaNode);

        ContainerSchemaNode moduleContainer = (ContainerSchemaNode) dataNode;
        DataSchemaNode dataChildList = moduleContainer
                .getDataChildByName("module");

        assertTrue(dataChildList instanceof ListSchemaNode);

        ListSchemaNode listModule = (ListSchemaNode) dataChildList;
        DataSchemaNode dataChildChoice = listModule
                .getDataChildByName("configuration");

        assertTrue(dataChildChoice instanceof ChoiceSchemaNode);

        ChoiceSchemaNode confChoice = (ChoiceSchemaNode) dataChildChoice;
        ChoiceCaseNode caseNodeByName = confChoice
                .getCaseNodeByName("dom-broker-impl");

        assertNotNull(caseNodeByName);
        DataSchemaNode dataNode2 = caseNodeByName
                .getDataChildByName("async-data-broker");
        assertTrue(dataNode2 instanceof ContainerSchemaNode);

        ContainerSchemaNode containerNode = (ContainerSchemaNode) dataNode2;
        DataSchemaNode leaf = containerNode.getDataChildByName("type");
        List<UnknownSchemaNode> unknownSchemaNodes = leaf
                .getUnknownSchemaNodes();

        assertEquals(1, unknownSchemaNodes.size());

        UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.get(0);

        assertEquals(unknownSchemaNode.getQName(), unknownSchemaNode.getPath()
                .getLastComponent());
        assertEquals("dom-async-data-broker", unknownSchemaNode.getQName()
                .getLocalName());

        ChoiceCaseNode domInmemoryDataBroker = confChoice
                .getCaseNodeByName("dom-inmemory-data-broker");

        assertNotNull(domInmemoryDataBroker);
        DataSchemaNode schemaService = domInmemoryDataBroker
                .getDataChildByName("schema-service");
        assertTrue(schemaService instanceof ContainerSchemaNode);

        ContainerSchemaNode schemaServiceContainer = (ContainerSchemaNode) schemaService;

        assertEquals(1, schemaServiceContainer.getUses().size());
        UsesNode uses = schemaServiceContainer.getUses().iterator().next();
        QName groupingQName = QName.create("urn:opendaylight:params:xml:ns:yang:controller:config","2013-04-05","service-ref");
        QName usesGroupingPathLastComponent = uses.getGroupingPath().getLastComponent();
        assertEquals(groupingQName, usesGroupingPathLastComponent);
        assertEquals(0, getChildNodeSizeWithoutUses(schemaServiceContainer));

        DataSchemaNode type = schemaServiceContainer.getDataChildByName("type");
        List<UnknownSchemaNode> typeUnknownSchemaNodes = type
                .getUnknownSchemaNodes();

        assertEquals(1, typeUnknownSchemaNodes.size());

        UnknownSchemaNode typeUnknownSchemaNode = typeUnknownSchemaNodes.get(0);

        QNameModule qNameModule = QNameModule
                .create(new URI(
                        "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:impl"),
                        SimpleDateFormatUtil.getRevisionFormat().parse(
                                "2013-10-28"));
        QName qName = QName.create(qNameModule, "schema-service");

        assertEquals(qName, typeUnknownSchemaNode.getQName());
        assertEquals(typeUnknownSchemaNode.getQName(), typeUnknownSchemaNode
                .getPath().getLastComponent());
    }

    private int getChildNodeSizeWithoutUses(final DataNodeContainer csn) {
        int result = 0;
        for (DataSchemaNode dsn : csn.getChildNodes()) {
            if (dsn.isAddedByUses() == false) {
                result++;
            }
        }
        return result;
    }

}
