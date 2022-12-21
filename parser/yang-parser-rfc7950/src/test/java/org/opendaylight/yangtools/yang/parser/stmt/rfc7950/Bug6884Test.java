/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6884Test extends AbstractYangTest {
    @Test
    void testYang11() {
        final var schemaContext = assertEffectiveModelDir("/rfc7950/bug6884/yang1-1");
        final DataSchemaNode node = schemaContext.findDataTreeChild(foo("sub-root"), foo("sub-foo-2-con")).orElse(null);
        assertInstanceOf(ContainerSchemaNode.class, node);
    }

    @Test
    void testCircularIncludesYang10() {
        final var schemaContext = assertEffectiveModelDir("/rfc7950/bug6884/circular-includes");
        DataSchemaNode node = schemaContext.findDataTreeChild(foo("sub-root"), foo("sub-foo-2-con")).orElse(null);
        assertInstanceOf(ContainerSchemaNode.class, node);

        node = schemaContext.findDataTreeChild(foo("sub-root-2"), foo("sub-foo-con")).orElse(null);
        assertInstanceOf(ContainerSchemaNode.class, node);
    }

    private static QName foo(final String localName) {
        return QName.create("foo", localName);
    }
}
