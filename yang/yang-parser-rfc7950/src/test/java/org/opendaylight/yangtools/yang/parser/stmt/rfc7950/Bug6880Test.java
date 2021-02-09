/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6880Test {
    @Test
    public void valid10Test() throws Exception {
        final EffectiveModelContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6880/foo.yang");
        assertNotNull(schemaContext);

        final DataSchemaNode findDataSchemaNode = schemaContext.findDataTreeChild(QName.create("foo", "my-leaf-list"))
            .orElse(null);
        assertThat(findDataSchemaNode, instanceOf(LeafListSchemaNode.class));
        final LeafListSchemaNode myLeafList = (LeafListSchemaNode) findDataSchemaNode;

        final Collection<? extends Object> defaults = myLeafList.getDefaults();
        assertEquals(2, defaults.size());
        assertTrue(defaults.contains("my-default-value-1") && defaults.contains("my-default-value-2"));
    }

    @Test
    public void invalid10Test() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6880/invalid10.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("DEFAULT is not valid for LEAF_LIST"));
    }
}