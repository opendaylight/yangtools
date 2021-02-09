/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6876Test {
    @Test
    public void yang11Test() throws Exception {
        final EffectiveModelContext context = StmtTestUtils.parseYangSources("/rfc7950/bug6876/yang11");
        DataSchemaNode node = context.findDataTreeChild(bar("augment-target"), bar("my-leaf")).orElse(null);
        assertThat(node, instanceOf(LeafSchemaNode.class));
        node = context.findDataTreeChild(bar("augment-target"), foo("mandatory-leaf")).orElse(null);
        assertThat(node, instanceOf(LeafSchemaNode.class));
    }

    @Test
    public void yang10Test() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/rfc7950/bug6876/yang10"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InferenceException.class));
        assertThat(cause.getMessage(), startsWith(
            "An augment cannot add node 'mandatory-leaf' because it is mandatory and in module different than target"));
    }

    private static QName foo(final String localName) {
        return QName.create("foo", localName);
    }

    private static QName bar(final String localName) {
        return QName.create("bar", "2017-01-11", localName);
    }
}
