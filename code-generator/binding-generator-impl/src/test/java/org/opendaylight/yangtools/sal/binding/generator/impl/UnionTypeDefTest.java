/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class UnionTypeDefTest {

    @Test
    public void unionTypeResolvingTest() throws Exception {
        File abstractTopology = new File(getClass().getResource("/union-test-models/abstract-topology.yang").toURI());
        File ietfInetTypes = new File(getClass().getResource("/ietf/ietf-inet-types.yang").toURI());
        final SchemaContext context = new YangParserImpl().parseFiles(Arrays.asList(abstractTopology, ietfInetTypes));

        assertNotNull("context is null", context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        assertNotNull("genTypes is null", genTypes);
        assertFalse("genTypes is empty", genTypes.isEmpty());

        // TODO: implement test
    }
}
