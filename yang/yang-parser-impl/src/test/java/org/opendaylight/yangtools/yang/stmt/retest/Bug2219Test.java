/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.CopyUtils;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;

/**
 * NPE is rised during use of CopyUtils.copy operation for IdentityrefTypeBuilder.
 * NPE occours in private getData method in CopyUtils.java during QName.create.
 *
 * The reason for exception is the old.getQName returns null since IdentityrefTypeBuilder.getQName()
 * by implementation returns always null.
 *
 */
public class Bug2219Test {

    private ModuleBuilder moduleBuilder;

    @Before
    public void init() {
        moduleBuilder = new ModuleBuilder("test-module", "somePath");
    }

    @Test
    public void testCopyIdentityrefTypeBuilder() {
        final String typedefLocalName = "identity-ref-test-type";
        final QName typedefQname = QName.create(moduleBuilder.getNamespace(), moduleBuilder.getRevision(), typedefLocalName);
        final SchemaPath typedefPath = SchemaPath.create(true, typedefQname);
        final IdentityrefTypeBuilder typeBuilder = new IdentityrefTypeBuilder(moduleBuilder.getModuleName(), 12,
            "base:parent-identity", typedefPath);

        final TypeDefinitionBuilder copy = CopyUtils.copy(typeBuilder, moduleBuilder, true);
        assertNotNull(copy);
    }
}
