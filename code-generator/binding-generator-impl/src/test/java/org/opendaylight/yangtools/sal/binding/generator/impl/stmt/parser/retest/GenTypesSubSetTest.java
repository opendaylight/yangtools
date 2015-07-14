/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl.stmt.parser.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class GenTypesSubSetTest {

    @Test
    public void genTypesFromSubsetOfTwoModulesTest() throws Exception {
        File abstractTopology = new File(getClass().getResource(
                "/leafref-test-models/abstract-topology@2013-02-08.yang").toURI());
        File ietfInterfaces = new File(getClass().getResource("/ietf/ietf-interfaces.yang").toURI());
        File ietfInetTypes = new File(getClass().getResource("/ietf/ietf-inet-types.yang").toURI());
        File ietfYangTypes = new File(getClass().getResource("/ietf/ietf-yang-types.yang").toURI());

        final SchemaContext context = RetestUtils.parseYangSources(abstractTopology, ietfInterfaces,
                ietfInetTypes, ietfYangTypes);
        Set<Module> modules = context.getModules();

        final Set<Module> toGenModules = new HashSet<>();
        for (final Module module : modules) {
            if (module.getName().equals("abstract-topology")) {
                toGenModules.add(module);
            } else if (module.getName().equals("ietf-interfaces")) {
                toGenModules.add(module);
            }
        }

        assertEquals("Set of to Generate Modules must contain 2 modules", 2, toGenModules.size());
        assertNotNull("Schema Context is null", context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl(true);
        final List<Type> genTypes = bindingGen.generateTypes(context, toGenModules);
        assertNotNull("genTypes is null", genTypes);
        assertFalse("genTypes is empty", genTypes.isEmpty());
        assertEquals("Expected Generated Types from provided sub set of " + "modules should be 23!", 23,
                genTypes.size());
    }

    @Test
    public void genTypesFromSubsetOfThreeModulesTest() throws Exception {
        File abstractTopology = new File(getClass().getResource(
                "/leafref-test-models/abstract-topology@2013-02-08.yang").toURI());
        File ietfInterfaces = new File(getClass().getResource("/ietf/ietf-interfaces.yang").toURI());
        File ietfInetTypes = new File(getClass().getResource("/ietf/ietf-inet-types.yang").toURI());
        File ietfYangTypes = new File(getClass().getResource("/ietf/ietf-yang-types.yang").toURI());
        File ianaIfType = new File(getClass().getResource("/ietf/iana-if-type.yang").toURI());

        final SchemaContext context = RetestUtils.parseYangSources(abstractTopology, ietfInterfaces,
                ietfInetTypes, ietfYangTypes, ianaIfType);
        assertNotNull("Schema Context is null", context);
        final Set<Module> modules = context.getModules();

        final Set<Module> toGenModules = new HashSet<>();
        for (final Module module : modules) {
            if (module.getName().equals("abstract-topology")) {
                toGenModules.add(module);
            } else if (module.getName().equals("ietf-interfaces")) {
                toGenModules.add(module);
            } else if (module.getName().equals("iana-if-type")) {
                toGenModules.add(module);
            }
        }
        assertEquals("Set of to Generate Modules must contain 3 modules", 3, toGenModules.size());

        final BindingGenerator bindingGen = new BindingGeneratorImpl(true);
        final List<Type> genTypes = bindingGen.generateTypes(context, toGenModules);
        assertNotNull("genTypes is null", genTypes);
        assertFalse("genTypes is empty", genTypes.isEmpty());
        assertEquals("Expected Generated Types", 24, genTypes.size());
    }
}
