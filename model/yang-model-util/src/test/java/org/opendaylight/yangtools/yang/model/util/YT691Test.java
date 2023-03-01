/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.SimpleSchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT691Test {
    @Test
    void testGetAllModuleIdentifiers() {
        final SourceIdentifier foo = new SourceIdentifier("foo", "2016-01-01");
        final SourceIdentifier sub1Foo = new SourceIdentifier("sub1-foo", "2016-01-01");
        final SourceIdentifier sub2Foo = new SourceIdentifier("sub2-foo", "2016-01-01");
        final SourceIdentifier bar = new SourceIdentifier("bar", "2016-01-01");
        final SourceIdentifier sub1Bar = new SourceIdentifier("sub1-bar", "2016-01-01");
        final SourceIdentifier baz = new SourceIdentifier("baz", "2016-01-01");
        final Set<SourceIdentifier> testSet = ImmutableSet.of(foo, sub1Foo, sub2Foo, bar, sub1Bar, baz);
        final EffectiveModelContext context = YangParserTestUtils.parseYangResourceDirectory("/yt691");
        final Set<SourceIdentifier> allModuleIdentifiers = SchemaContextUtil.getConstituentModuleIdentifiers(context);
        assertEquals(6, allModuleIdentifiers.size());
        final Set<SourceIdentifier> allModuleIdentifiersResolved = SchemaContextUtil.getConstituentModuleIdentifiers(
                SimpleSchemaContext.forModules(context.getModules()));
        assertEquals(6, allModuleIdentifiersResolved.size());
        assertEquals(allModuleIdentifiersResolved, allModuleIdentifiers);
        assertEquals(allModuleIdentifiers, testSet);
        assertTrue(allModuleIdentifiers.contains(foo));
    }
}