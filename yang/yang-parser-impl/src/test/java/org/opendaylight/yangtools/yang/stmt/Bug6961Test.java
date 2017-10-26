/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.util.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.SimpleSchemaContext;

public class Bug6961Test {

    @Test
    public void testBug6961SchemaContext() throws Exception {
        final Optional<Revision> revision = Revision.ofNullable("2016-01-01");
        final ModuleIdentifier foo = ModuleIdentifierImpl.create("foo", revision);
        final ModuleIdentifier sub1Foo = ModuleIdentifierImpl.create("sub1-foo", revision);
        final ModuleIdentifier sub2Foo = ModuleIdentifierImpl.create("sub2-foo", revision);
        final ModuleIdentifier bar = ModuleIdentifierImpl.create("bar", revision);
        final ModuleIdentifier sub1Bar = ModuleIdentifierImpl.create("sub1-bar", revision);
        final ModuleIdentifier baz = ModuleIdentifierImpl.create("baz", revision);
        final Set<ModuleIdentifier> testSet = ImmutableSet.of(foo, sub1Foo, sub2Foo, bar, sub1Bar, baz);
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6961/");
        assertNotNull(context);
        final Set<ModuleIdentifier> allModuleIdentifiers = SchemaContextUtil.getConstituentModuleIdentifiers(context);
        assertNotNull(allModuleIdentifiers);
        assertEquals(6, allModuleIdentifiers.size());
        final SchemaContext schemaContext = SimpleSchemaContext.forModules(context.getModules());
        assertNotNull(schemaContext);
        final Set<ModuleIdentifier> allModuleIdentifiersResolved = SchemaContextUtil.getConstituentModuleIdentifiers(
            schemaContext);
        assertNotNull(allModuleIdentifiersResolved);
        assertEquals(6, allModuleIdentifiersResolved.size());
        assertEquals(allModuleIdentifiersResolved, allModuleIdentifiers);
        assertEquals(allModuleIdentifiers, testSet);
        assertTrue(allModuleIdentifiers.contains(foo));
    }
}