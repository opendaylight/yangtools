/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl.stmt.parser.retest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import java.io.File;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class ControllerTest {

    @Test
    public void controllerAugmentationTest() throws Exception {
        File cn = new File(getClass().getResource("/controller-models/controller-network.yang").toURI());
        File co = new File(getClass().getResource("/controller-models/controller-openflow.yang").toURI());
        File ietfInetTypes = new File(getClass().getResource("/ietf/ietf-inet-types.yang").toURI());

        final SchemaContext context =  RetestUtils.parseYangSources(cn, co, ietfInetTypes);
        assertNotNull("Schema Context is null", context);

        final BindingGenerator bindingGen = new BindingGeneratorImpl(true);
        final List<Type> genTypes = bindingGen.generateTypes(context);

        assertNotNull(genTypes);
        assertTrue(!genTypes.isEmpty());
    }
}
