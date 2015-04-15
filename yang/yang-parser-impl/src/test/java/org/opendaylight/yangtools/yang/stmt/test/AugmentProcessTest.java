package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

import java.net.URI;

public class AugmentProcessTest {

    private static final YangStatementSourceImpl AUGMENTED = new YangStatementSourceImpl(
            "/stmt-test/effective-build/augmented.yang");
    private static final YangStatementSourceImpl ROOT = new YangStatementSourceImpl(
            "/stmt-test/effective-build/aug-root.yang");

    private static final QNameModule AUGMENTED_QNAME_MODULE = QNameModule.create(URI.create("aug"), null);

    QName augParent1 = QName.create(AUGMENTED_QNAME_MODULE, "aug-parent1");
    QName augParent2 = QName.create(AUGMENTED_QNAME_MODULE, "aug-parent2");
    QName contTarget = QName.create(AUGMENTED_QNAME_MODULE, "cont-target");

    QName contAdded1 = QName.create(AUGMENTED_QNAME_MODULE, "cont-added1");
    QName contAdded2 = QName.create(AUGMENTED_QNAME_MODULE, "cont-added2");

    QName list1 = QName.create(AUGMENTED_QNAME_MODULE, "list1");
    QName axml = QName.create(AUGMENTED_QNAME_MODULE, "axml");

    QName contGrp = QName.create(AUGMENTED_QNAME_MODULE, "cont-grp");
    QName axmlGrp = QName.create(AUGMENTED_QNAME_MODULE, "axml-grp");

    @Test
    public void readAndParseYangFileTest() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, AUGMENTED, ROOT);

        final EffectiveSchemaContext result = reactor.buildEffective();

        assertNotNull(result);

        Module augmentedModule = result.findModuleByName("augmented", null);
        assertNotNull(augmentedModule);

        ContainerSchemaNode augParent1Node = (ContainerSchemaNode) result.getDataChildByName(augParent1);
        ContainerSchemaNode augParent2Node = (ContainerSchemaNode) augParent1Node.getDataChildByName(augParent2);
        ContainerSchemaNode targetContNode = (ContainerSchemaNode) augParent2Node.getDataChildByName(contTarget);
        assertNotNull(targetContNode);

        assertNotNull(targetContNode.getChildNodes());
        assertEquals(3, targetContNode.getChildNodes().size());

        ContainerSchemaNode contAdded1Node = (ContainerSchemaNode) targetContNode.getDataChildByName(contAdded1);
        assertNotNull(contAdded1Node);
        ListSchemaNode list1Node = (ListSchemaNode) contAdded1Node.getDataChildByName(list1);
        assertNotNull(list1Node);

        ContainerSchemaNode contAdded2Node = (ContainerSchemaNode) targetContNode.getDataChildByName(contAdded2);
        assertNotNull(contAdded2Node);
        AnyXmlSchemaNode axmlNode = (AnyXmlSchemaNode) contAdded2Node.getDataChildByName(axml);
        assertNotNull(axmlNode);

        ContainerSchemaNode contGrpNode = (ContainerSchemaNode) targetContNode.getDataChildByName(contGrp);
        assertNotNull(contGrpNode);
        AnyXmlSchemaNode axmlGrpNode = (AnyXmlSchemaNode) contGrpNode.getDataChildByName(axmlGrp);
        assertNotNull(axmlGrpNode);
    }

    private void addSources(CrossSourceStatementReactor.BuildAction reactor, StatementStreamSource... sources) {
        for (StatementStreamSource source : sources) {
            reactor.addSource(source);
        }
    }
}
