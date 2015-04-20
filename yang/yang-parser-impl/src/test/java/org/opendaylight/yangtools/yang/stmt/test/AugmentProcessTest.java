package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.meta.ModelStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

import com.google.common.collect.ImmutableList;

public class AugmentProcessTest {

    private static final YangStatementSourceImpl AUGMENTED = new YangStatementSourceImpl(
            "/stmt-test/effective-build/augmented.yang");
    private static final YangStatementSourceImpl ROOT = new YangStatementSourceImpl(
            "/stmt-test/effective-build/aug-root.yang");

    private static final QNameModule ROOT_QNAME_MODULE = QNameModule.create(URI.create("root"), null);
    private static final QNameModule AUGMENTED_QNAME_MODULE = QNameModule.create(URI.create("aug"), null);

    private static GroupingDefinition grp2Def;

    private final QName augParent1 = QName.create(AUGMENTED_QNAME_MODULE, "aug-parent1");
    private final QName augParent2 = QName.create(AUGMENTED_QNAME_MODULE, "aug-parent2");
    private final QName contTarget = QName.create(AUGMENTED_QNAME_MODULE, "cont-target");

    private final QName contAdded1 = QName.create(AUGMENTED_QNAME_MODULE, "cont-added1");
    private final QName contAdded2 = QName.create(AUGMENTED_QNAME_MODULE, "cont-added2");

    private final QName list1 = QName.create(AUGMENTED_QNAME_MODULE, "list1");
    private final QName axml = QName.create(AUGMENTED_QNAME_MODULE, "axml");

    private final QName contGrp = QName.create(AUGMENTED_QNAME_MODULE, "cont-grp");
    private final QName axmlGrp = QName.create(AUGMENTED_QNAME_MODULE, "axml-grp");

    private final QName grp2 = QName.create(ROOT_QNAME_MODULE, "grp2");
    private final QName grpCont2 = QName.create(ROOT_QNAME_MODULE, "grp-cont2");
    private final QName grpCont22 = QName.create(ROOT_QNAME_MODULE, "grp-cont22");

    @Test
    public void readAndParseYangFileTest() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, AUGMENTED, ROOT);

        final EffectiveSchemaContext root = reactor.buildEffective();
        assertNotNull(root);

        Module augmentedModule = root.findModuleByName("augmented", null);
        assertNotNull(augmentedModule);

        ContainerSchemaNode augParent1Node = (ContainerSchemaNode) root.getDataChildByName(augParent1);
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

        for (GroupingDefinition grouping : root.getGroupings()) {
            if (grouping.getQName().equals(grp2)) {
                grp2Def = grouping;
                break;
            }
        }

        ContainerSchemaNode grpCont2Node = (ContainerSchemaNode) grp2Def.getDataChildByName(grpCont2);
        ContainerSchemaNode grpCont22Node = (ContainerSchemaNode) grpCont2Node.getDataChildByName(grpCont22);

        assertNotNull(grpCont22Node);
    }

    private <T extends ModelStatement> T findInStatements(QName target, ImmutableList<T> statements) {

        for (final T statement : statements) {
            if (target.equals(statement.statementDefinition().getArgumentName())) {
                return statement;
            }
        }

        return null;
    }

    private void addSources(CrossSourceStatementReactor.BuildAction reactor, StatementStreamSource... sources) {
        for (StatementStreamSource source : sources) {
            reactor.addSource(source);
        }
    }
}
