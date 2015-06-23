package org.opendaylight.yangtools.yang.stmt.effective.build.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import java.util.Set;
import java.net.URISyntaxException;
import java.io.FileNotFoundException;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class EffectiveBuildTest {

    private static final YangStatementSourceImpl SIMPLE_MODULE = new YangStatementSourceImpl(
            "/stmt-test/effective-build/simple-module.yang", false);
    private static final QNameModule SIMPLE_MODULE_QNAME = QNameModule.create(
            URI.create("simple.yang"), SimpleDateFormatUtil.DEFAULT_DATE_REV);
    private static final YangStatementSourceImpl YANG_EXT = new YangStatementSourceImpl(
            "/stmt-test/extensions/yang-ext.yang", false);

    @Test
    public void effectiveBuildTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, SIMPLE_MODULE);
        EffectiveSchemaContext result = reactor.buildEffective();

        assertNotNull(result);

        Module simpleModule = result.findModuleByName("simple-module", null);
        assertNotNull(simpleModule);

        QName q1 = QName.create(SIMPLE_MODULE_QNAME, "root-container");
        QName q2 = QName.create(SIMPLE_MODULE_QNAME, "sub-container");
        QName q3 = QName.create(SIMPLE_MODULE_QNAME, "sub-sub-container");
        QName q4 = QName.create(SIMPLE_MODULE_QNAME, "root-container2");
        QName q5 = QName.create(SIMPLE_MODULE_QNAME, "sub-container2");
        QName q6 = QName.create(SIMPLE_MODULE_QNAME, "sub-sub-container2");
        QName q7 = QName.create(SIMPLE_MODULE_QNAME, "grp");

        ContainerSchemaNode rootCon = (ContainerSchemaNode) simpleModule
                .getDataChildByName(q1);
        assertNotNull(rootCon);

        ContainerSchemaNode subCon = (ContainerSchemaNode) rootCon
                .getDataChildByName(q2);
        assertNotNull(subCon);

        ContainerSchemaNode subSubCon = (ContainerSchemaNode) subCon
                .getDataChildByName(q3);
        assertNotNull(subSubCon);

        ContainerSchemaNode rootCon2 = (ContainerSchemaNode) simpleModule
                .getDataChildByName(q4);
        assertNotNull(rootCon2);

        ContainerSchemaNode subCon2 = (ContainerSchemaNode) rootCon2
                .getDataChildByName(q5);
        assertNotNull(subCon2);

        ContainerSchemaNode subSubCon2 = (ContainerSchemaNode) subCon2
                .getDataChildByName(q6);
        assertNotNull(subSubCon2);

        GroupingDefinition grp = simpleModule.getGroupings().iterator().next();
        assertNotNull(grp);
        assertEquals(q7, grp.getQName());

        ContainerSchemaNode grpSubCon2 = (ContainerSchemaNode) grp
                .getDataChildByName(q5);
        assertNotNull(grpSubCon2);

        ContainerSchemaNode grpSubSubCon2 = (ContainerSchemaNode) grpSubCon2
                .getDataChildByName(q6);
        assertNotNull(grpSubSubCon2);

        assertEquals(SchemaPath.create(true, q1, q2, q3), subSubCon.getPath());
        assertEquals(SchemaPath.create(true, q4, q5, q6), subSubCon2.getPath());
        assertEquals(SchemaPath.create(true, q7, q5, q6),
                grpSubSubCon2.getPath());

    }

    @Test
    public void extensionsTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, YANG_EXT);
        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Set<GroupingDefinition> groupings = result.getGroupings();
        assertEquals(1, groupings.size());

        GroupingDefinition grp = groupings.iterator().next();

        Collection<DataSchemaNode> childNodes = grp.getChildNodes();
        assertEquals(1, childNodes.size());
        DataSchemaNode child = childNodes.iterator().next();

        assertTrue(child instanceof LeafSchemaNode);
        LeafSchemaNode leaf = (LeafSchemaNode) child;

        assertNotNull(leaf.getType());
    }

    @Test
    public void mockTest() throws SourceException, ReactorException, FileNotFoundException, URISyntaxException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSource(YANG_EXT);

        SchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    private void addSources(BuildAction reactor,
            YangStatementSourceImpl... sources) {
        for (YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }
}
