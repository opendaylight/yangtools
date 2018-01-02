/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;

import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

public class YT822Test {
    private static final QNameModule IETF_ROUTING = QNameModule.create(
        URI.create("urn:ietf:params:xml:ns:yang:ietf-routing"), QName.parseRevision("2016-08-18"));

    @Test
    public void testImplicitActionInput() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/YT822/");
        final Module foo = context.findModuleByNamespace(URI.create("urn:ietf:params:xml:ns:yang:ietf-mpls"))
                .iterator().next();
        assertNotNull(foo);

        final SchemaNode target = SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true,
            QName.create(IETF_ROUTING, "routing-state"),
            QName.create(IETF_ROUTING, "ribs"),
            QName.create(IETF_ROUTING, "rib"),
            QName.create(IETF_ROUTING, "active-route"),
            QName.create(IETF_ROUTING, "input")));
        assertNotNull(target);

    }
}
