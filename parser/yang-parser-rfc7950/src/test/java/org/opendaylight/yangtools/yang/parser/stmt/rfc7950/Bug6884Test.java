/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6884Test {
    @Test
    public void testYang11() throws Exception {
        final EffectiveModelContext schemaContext = StmtTestUtils.parseYangSources("/rfc7950/bug6884/yang1-1");
        final DataSchemaNode node = schemaContext.findDataTreeChild(foo("sub-root"), foo("sub-foo-2-con")).orElse(null);
        assertThat(node, instanceOf(ContainerSchemaNode.class));
    }

    @Test
    public void testCircularIncludesYang10() throws Exception {
        final EffectiveModelContext schemaContext =
            StmtTestUtils.parseYangSources("/rfc7950/bug6884/circular-includes");
        DataSchemaNode node = schemaContext.findDataTreeChild(foo("sub-root"), foo("sub-foo-2-con")).orElse(null);
        assertThat(node, instanceOf(ContainerSchemaNode.class));

        node = schemaContext.findDataTreeChild(foo("sub-root-2"), foo("sub-foo-con")).orElse(null);
        assertThat(node, instanceOf(ContainerSchemaNode.class));
    }

    private static QName foo(final String localName) {
        return QName.create("foo", localName);
    }
}
