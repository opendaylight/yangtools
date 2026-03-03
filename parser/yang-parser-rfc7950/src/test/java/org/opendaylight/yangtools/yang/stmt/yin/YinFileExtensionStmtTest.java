/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.Module;

class YinFileExtensionStmtTest extends AbstractYinModulesTest {

    @Test
    void testExtensions() {
        Module testModule = context.findModules("config").iterator().next();
        assertNotNull(testModule);

        var extensions = testModule.getExtensionSchemaNodes();
        assertEquals(5, extensions.size());

        var extIterator = extensions.iterator();
        var extension = extIterator.next();
        assertEquals("name", extension.getArgument());
        assertEquals("java-class", extension.getQName().getLocalName());
        assertEquals(Optional.of("""
            YANG language extension carrying the fully-qualified name of
            a Java class. Code generation tools use the provided reference
            to tie a specific construct to its Java representation."""), extension.getDescription());

        extension = extIterator.next();
        assertEquals("name", extension.getArgument());
        assertEquals("required-identity", extension.getQName().getLocalName());
        assertEquals(Optional.of("""
            YANG language extension which indicates that a particular
            leafref, which points to a identityref, should additionally
            require the target node is actually set to a descendant to
            of a particular identity.

            This is a workaround to two YANG deficiencies:
            1) not being able to leafref instances of identityref
            2) not being able to refine an identityref

            This extension takes one argument, name, which MUST be the name
            of an identity. Furthermore, that identity MUST be based,
            directly or indirectly, on the identity, which is referenced by
            the leaf reference, which is annotated with this extension."""), extension.getDescription());

        extension = extIterator.next();
        assertNull(extension.getArgument());
        assertEquals("inner-state-bean", extension.getQName().getLocalName());
        assertEquals(Optional.of("""
            YANG language extension which indicates that a particular
            list located under module's state should be treated as a list
            of child state beans instead of just an ordinary list attribute"""), extension.getDescription());

        extension = extIterator.next();
        assertEquals("name", extension.getArgument());
        assertEquals("provided-service", extension.getQName().getLocalName());
        assertEquals(Optional.of("""
            YANG language extension which indicates that a particular
            module provides certain service. This extension can be placed
            on identities that are based on module-type. Zero or more services
            can be provided.
            This extension takes one argument - name - which MUST be the name
            of an identity. Furthermore, this identity MUST be based on
            service-type."""), extension.getDescription());

        extension = extIterator.next();
        assertEquals("java-prefix", extension.getArgument());
        assertEquals("java-name-prefix", extension.getQName().getLocalName());
        assertEquals(Optional.of("""
            YANG language extension carrying java simple class name prefix
            that will be taken into account when generating java code from
            identities that are based on module-type."""), extension.getDescription());
    }
}
