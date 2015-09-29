package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class NotificationStmtTest {

    private static final YangStatementSourceImpl NOTIFICATION_MODULE = new YangStatementSourceImpl("/model/baz.yang",
            false);
    private static final YangStatementSourceImpl IMPORTED_MODULE = new YangStatementSourceImpl("/model/bar.yang",
            false);

    @Test
    public void notificationTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, NOTIFICATION_MODULE, IMPORTED_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("baz", null);
        assertNotNull(testModule);

        Set<NotificationDefinition> notifications = testModule.getNotifications();
        assertEquals(1, notifications.size());

        NotificationDefinition notification = notifications.iterator().next();
        assertEquals("event", notification.getQName().getLocalName());
        assertEquals(3, notification.getChildNodes().size());

        LeafSchemaNode leaf = (LeafSchemaNode) notification.getDataChildByName("event-class");
        assertNotNull(leaf);
        leaf = (LeafSchemaNode) notification.getDataChildByName("severity");
        assertNotNull(leaf);
        AnyXmlSchemaNode anyXml = (AnyXmlSchemaNode) notification.getDataChildByName("reporting-entity");
        assertNotNull(anyXml);
    }
}
