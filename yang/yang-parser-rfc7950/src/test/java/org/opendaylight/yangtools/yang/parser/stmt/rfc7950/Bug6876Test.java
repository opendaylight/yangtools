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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6876Test {
    private static final String BAR_NS = "bar";
    private static final String BAR_REV = "2017-01-11";
    private static final String FOO_NS = "foo";

    @Test
    public void yang11Test() throws Exception {
        final EffectiveModelContext context = StmtTestUtils.parseYangSources("/rfc7950/bug6876/yang11");
        assertNotNull(context);

        assertThat(findNode(context, ImmutableList.of(bar("augment-target"), bar("my-leaf"))),
            instanceOf(LeafSchemaNode.class));
        assertThat(findNode(context, ImmutableList.of(bar("augment-target"), foo("mandatory-leaf"))),
            instanceOf(LeafSchemaNode.class));
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

    private static SchemaNode findNode(final EffectiveModelContext context, final Iterable<QName> qnames) {
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        qnames.forEach(stack::enterSchemaTree);
        return SchemaContextUtil.findDataSchemaNode(context, stack);
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, localName);
    }

    private static QName bar(final String localName) {
        return QName.create(BAR_NS, BAR_REV, localName);
    }
}
