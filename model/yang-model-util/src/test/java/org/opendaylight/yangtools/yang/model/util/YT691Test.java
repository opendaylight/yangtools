/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.SimpleSchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT691Test {
    @Test
    public void testGetAllModuleIdentifiers() {
        final Optional<Revision> revision = Revision.ofNullable("2016-01-01");
        final SourceIdentifier foo = RevisionSourceIdentifier.create("foo", revision);
        final SourceIdentifier sub1Foo = RevisionSourceIdentifier.create("sub1-foo", revision);
        final SourceIdentifier sub2Foo = RevisionSourceIdentifier.create("sub2-foo", revision);
        final SourceIdentifier bar = RevisionSourceIdentifier.create("bar", revision);
        final SourceIdentifier sub1Bar = RevisionSourceIdentifier.create("sub1-bar", revision);
        final SourceIdentifier baz = RevisionSourceIdentifier.create("baz", revision);
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