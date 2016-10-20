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

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Date;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class Bug6961Test {

    @Test
    public void testBug6961SchemaContext() throws Exception {
        final Optional<Date> date = Optional.of(SimpleDateFormatUtil.getRevisionFormat().parse("2016-01-01"));
        final ModuleIdentifierImpl foo = new ModuleIdentifierImpl("foo", Optional.of(new URI("foo")), date);
        final ModuleIdentifierImpl sub1Foo = new ModuleIdentifierImpl("sub1-foo", Optional.of(new URI("foo")), date);
        final ModuleIdentifierImpl sub2Foo = new ModuleIdentifierImpl("sub2-foo", Optional.of(new URI("foo")), date);
        final ModuleIdentifierImpl bar = new ModuleIdentifierImpl("bar", Optional.of(new URI("bar")), date);
        final ModuleIdentifierImpl sub1Bar = new ModuleIdentifierImpl("sub1-bar", Optional.of(new URI("bar")), date);
        final ModuleIdentifierImpl baz = new ModuleIdentifierImpl("baz", Optional.of(new URI("baz")), date);
        final Set<ModuleIdentifier> testSet = Sets.newHashSet(foo, sub1Foo, sub2Foo, bar, sub1Bar, baz);
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6961/");
        assertNotNull(context);
        final Set<ModuleIdentifier> allModuleIdentifiers = context.getAllModuleIdentifiers();
        assertNotNull(allModuleIdentifiers);
        assertEquals(6, allModuleIdentifiers.size());
        final SchemaContext schemaContext = EffectiveSchemaContext.resolveSchemaContext(context.getModules());
        assertNotNull(schemaContext);
        final Set<ModuleIdentifier> allModuleIdentifiersResolved = schemaContext.getAllModuleIdentifiers();
        assertNotNull(allModuleIdentifiersResolved);
        assertEquals(6, allModuleIdentifiersResolved.size());
        assertEquals(allModuleIdentifiersResolved, allModuleIdentifiers);
        assertEquals(allModuleIdentifiers, testSet);
        assertTrue(allModuleIdentifiers.contains(foo));
        final QNameModule fooQNameModule = foo.getQNameModule();
        final QNameModule fooQNameModuleCreated = QNameModule.create(new URI("foo"), date.orNull());
        assertEquals(fooQNameModule, fooQNameModuleCreated);
    }
}