package org.opendaylight.yangtools.yang.stmt.effective.build.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import java.net.URI;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class EffectiveBuildTest {

    private static final YangFileStatementSource SIMPLE_MODULE = new YangFileStatementSource("/stmt-test/effective-build/simple-module.yang");
    private static final QNameModule SIMPLE_MODULE_QNAME = QNameModule.create(URI.create("simple.yang"), null);

    @Test
    public void effectiveBuildTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, SIMPLE_MODULE);
        EffectiveSchemaContext result = reactor.buildEffective();

        assertNotNull(result);

        Module simpleModule = result.findModuleByName("simple-module",null);
        assertNotNull(simpleModule);

        QName q1 = QName.create(SIMPLE_MODULE_QNAME, "root-container");
        QName q2 = QName.create(SIMPLE_MODULE_QNAME, "sub-container");
        QName q3 = QName.create(SIMPLE_MODULE_QNAME, "sub-sub-container");
        QName q4 = QName.create(SIMPLE_MODULE_QNAME, "root-container2");
        QName q5 = QName.create(SIMPLE_MODULE_QNAME, "sub-container2");
        QName q6 = QName.create(SIMPLE_MODULE_QNAME, "sub-sub-container2");
        QName q7 = QName.create(SIMPLE_MODULE_QNAME, "grp");

        ContainerSchemaNode rootCon = (ContainerSchemaNode)simpleModule.getDataChildByName(q1);
        assertNotNull(rootCon);

        ContainerSchemaNode subCon = (ContainerSchemaNode)rootCon.getDataChildByName(q2);
        assertNotNull(subCon);

        ContainerSchemaNode subSubCon = (ContainerSchemaNode)subCon.getDataChildByName(q3);
        assertNotNull(subSubCon);

        ContainerSchemaNode rootCon2 = (ContainerSchemaNode)simpleModule.getDataChildByName(q4);
        assertNotNull(rootCon2);

        ContainerSchemaNode subCon2 = (ContainerSchemaNode)rootCon2.getDataChildByName(q5);
        assertNotNull(subCon2);

        ContainerSchemaNode subSubCon2 = (ContainerSchemaNode)subCon2.getDataChildByName(q6);
        assertNotNull(subSubCon2);

        GroupingDefinition grp = simpleModule.getGroupings().iterator().next();
        assertNotNull(grp);
        assertEquals(q7,grp.getQName());

        ContainerSchemaNode grpSubCon2 = (ContainerSchemaNode) grp.getDataChildByName(q5);
        assertNotNull(grpSubCon2);

        ContainerSchemaNode grpSubSubCon2 = (ContainerSchemaNode)grpSubCon2.getDataChildByName(q6);
        assertNotNull(grpSubSubCon2);

    }

    private void log(Throwable e, String indent) {
        System.out.println(indent + e.getMessage());

        Throwable[] suppressed = e.getSuppressed();
        for (Throwable throwable : suppressed) {
            log(throwable, indent + "        ");
        }

    }

    private void addSources(BuildAction reactor,
            YangFileStatementSource... sources) {
        for (YangFileStatementSource source : sources) {
            reactor.addSource(source);
        }
    }

}
