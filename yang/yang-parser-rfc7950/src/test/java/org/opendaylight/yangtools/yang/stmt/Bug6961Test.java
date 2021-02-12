/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.util.SimpleSchemaContext;

public class Bug6961Test {
    @Test
    public void testBug6961SchemaContext() throws Exception {
        final Optional<Revision> revision = Revision.ofNullable("2016-01-01");
        final SourceIdentifier foo = RevisionSourceIdentifier.create("foo", revision);
        final SourceIdentifier sub1Foo = RevisionSourceIdentifier.create("sub1-foo", revision);
        final SourceIdentifier sub2Foo = RevisionSourceIdentifier.create("sub2-foo", revision);
        final SourceIdentifier bar = RevisionSourceIdentifier.create("bar", revision);
        final SourceIdentifier sub1Bar = RevisionSourceIdentifier.create("sub1-bar", revision);
        final SourceIdentifier baz = RevisionSourceIdentifier.create("baz", revision);
        final Set<SourceIdentifier> testSet = ImmutableSet.of(foo, sub1Foo, sub2Foo, bar, sub1Bar, baz);
        final EffectiveModelContext context = StmtTestUtils.parseYangSources("/bugs/bug6961/");
        final Set<SourceIdentifier> allModuleIdentifiers = getConstituentModuleIdentifiers(context.getModules());
        assertEquals(6, allModuleIdentifiers.size());
        final Set<SourceIdentifier> allModuleIdentifiersResolved = getConstituentModuleIdentifiers(
            SimpleSchemaContext.forModules(context.getModules()).getModules());
        assertEquals(6, allModuleIdentifiersResolved.size());
        assertEquals(allModuleIdentifiersResolved, allModuleIdentifiers);
        assertEquals(allModuleIdentifiers, testSet);
        assertTrue(allModuleIdentifiers.contains(foo));
    }

    /**
     * Extract the identifiers of all modules and submodules which were used to create a particular SchemaContext.
     *
     * @param collection SchemaContext to be examined
     * @return Set of ModuleIdentifiers.
     */
    private static Set<SourceIdentifier> getConstituentModuleIdentifiers(final Collection<? extends Module> modules) {
        final Set<SourceIdentifier> ret = new HashSet<>();
        for (Module module : modules) {
            ret.add(moduleToIdentifier(module));
            for (Submodule submodule : module.getSubmodules()) {
                ret.add(moduleToIdentifier(submodule));
            }
        }
        return ret;
    }

    private static SourceIdentifier moduleToIdentifier(final ModuleLike module) {
        return RevisionSourceIdentifier.create(module.getName(), module.getRevision());
    }
}