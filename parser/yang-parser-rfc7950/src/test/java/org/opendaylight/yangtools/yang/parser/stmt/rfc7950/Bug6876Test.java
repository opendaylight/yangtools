/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6876Test extends AbstractYangTest {
    @Test
    void yang11Test() {
        final var context = assertEffectiveModelDir("/rfc7950/bug6876/yang11");
        DataSchemaNode node = context.findDataTreeChild(bar("augment-target"), bar("my-leaf")).orElse(null);
        assertInstanceOf(LeafSchemaNode.class, node);
        node = context.findDataTreeChild(bar("augment-target"), foo("mandatory-leaf")).orElse(null);
        assertInstanceOf(LeafSchemaNode.class, node);
    }

    @Test
    void yang10Test() {
        assertInferenceExceptionDir("/rfc7950/bug6876/yang10", startsWith(
            "An augment cannot add node 'mandatory-leaf' because it is mandatory and in module different than target"));
    }

    private static QName foo(final String localName) {
        return QName.create("foo", localName);
    }

    private static QName bar(final String localName) {
        return QName.create("bar", "2017-01-11", localName);
    }
}
