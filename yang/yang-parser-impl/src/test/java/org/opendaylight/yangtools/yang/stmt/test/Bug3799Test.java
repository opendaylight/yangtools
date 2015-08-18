package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;

public class Bug3799Test {

    @Test
    public void test() throws IOException, URISyntaxException, SourceException,
            ReactorException {
        SchemaContext schema = StmtTestUtils.parseYangSources("/bugs/bug3799");
        assertNotNull(schema);

        Set<Module> modules = schema.getModules();
        assertNotNull(modules);
        assertEquals(1, modules.size());

        Module testModule = modules.iterator().next();
        Set<Module> subModules = testModule.getSubmodules();
        assertNotNull(subModules);
        assertEquals(1, subModules.size());

        Module testSubmodule = subModules.iterator().next();

        Set<NotificationDefinition> notifications = testSubmodule
                .getNotifications();
        assertNotNull(notifications);
        assertEquals(1, notifications.size());

        NotificationDefinition bazNotification = notifications.iterator()
                .next();
        Collection<DataSchemaNode> childNodes = bazNotification.getChildNodes();
        assertNotNull(childNodes);
        assertEquals(1, childNodes.size());

        DataSchemaNode child = childNodes.iterator().next();
        assertTrue(child instanceof LeafSchemaNode);

        LeafSchemaNode leafBar = (LeafSchemaNode) child;
        String bar = leafBar.getQName().getLocalName();
        assertEquals("bar", bar);
    }

}
