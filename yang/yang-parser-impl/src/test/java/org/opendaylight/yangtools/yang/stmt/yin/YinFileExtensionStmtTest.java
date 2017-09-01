/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.TestUtils;
import org.xml.sax.SAXException;

public class YinFileExtensionStmtTest {

    private SchemaContext context;

    @Before
    public void init() throws ReactorException, SAXException, IOException, URISyntaxException {
        context = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(9, context.getModules().size());
    }

    @Test
    public void testExtensions() {
        Module testModule = TestUtils.findModule(context, "config").get();
        assertNotNull(testModule);

        List<ExtensionDefinition> extensions = testModule.getExtensionSchemaNodes();
        assertEquals(5, extensions.size());

        Iterator<ExtensionDefinition> extIterator = extensions.iterator();
        ExtensionDefinition extension = extIterator.next();
        assertEquals("name", extension.getArgument());
        assertEquals("java-class", extension.getQName().getLocalName());
        assertEquals("YANG language extension carrying the fully-qualified name of\n" +
                "a Java class. Code generation tools use the provided reference\n" +
                "to tie a specific construct to its Java representation.", extension.getDescription());

        extension = extIterator.next();
        assertEquals("name", extension.getArgument());
        assertEquals("required-identity", extension.getQName().getLocalName());
        assertEquals("YANG language extension which indicates that a particular\n" +
                "leafref, which points to a identityref, should additionally\n" +
                "require the target node is actually set to a descendant to\n" +
                "of a particular identity.\n" +
                "\n" +
                "This is a workaround to two YANG deficiencies:\n" +
                "1) not being able to leafref instances of identityref\n" +
                "2) not being able to refine an identityref\n" +
                "\n" +
                "This extension takes one argument, name, which MUST be the name\n" +
                "of an identity. Furthermore, that identity MUST be based,\n" +
                "directly or indirectly, on the identity, which is referenced by\n" +
                "the leaf reference, which is annotated with this extension.", extension.getDescription());

        extension = extIterator.next();
        assertNull(extension.getArgument());
        assertEquals("inner-state-bean", extension.getQName().getLocalName());
        assertEquals("YANG language extension which indicates that a particular\n" +
                "list located under module's state should be treated as a list\n" +
                "of child state beans instead of just an ordinary list attribute", extension.getDescription());

        extension = extIterator.next();
        assertEquals("name", extension.getArgument());
        assertEquals("provided-service", extension.getQName().getLocalName());
        assertEquals("YANG language extension which indicates that a particular\n" +
                "module provides certain service. This extension can be placed\n" +
                "on identities that are based on module-type. Zero or more services\n" +
                "can be provided.\n" +
                "This extension takes one argument - name - which MUST be the name\n" +
                "of an identity. Furthermore, this identity MUST be based on\n" +
                "service-type.", extension.getDescription());

        extension = extIterator.next();
        assertEquals("java-prefix", extension.getArgument());
        assertEquals("java-name-prefix", extension.getQName().getLocalName());
        assertEquals("YANG language extension carrying java simple class name prefix\n" +
                "that will be taken into account when generating java code from\n" +
                "identities that are based on module-type.", extension.getDescription());
    }

}
