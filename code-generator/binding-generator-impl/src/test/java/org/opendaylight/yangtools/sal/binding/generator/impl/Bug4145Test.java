/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import java.net.URISyntaxException;
import java.io.File;
import java.util.List;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.junit.Test;

public class Bug4145Test {
    @Test
    public void bug4145Test() throws URISyntaxException, IOException, YangSyntaxErrorException {
        File resourceFile = new File(getClass().getResource(
                "/bug-4145/foo.yang").toURI());
        File resourceDir = resourceFile.getParentFile();

        YangParserImpl parser = YangParserImpl.getInstance();
        SchemaContext context = parser.parseFile(resourceFile, resourceDir);

        List<Type> generateTypes = new BindingGeneratorImpl(false)
                .generateTypes(context);
        assertNotNull(generateTypes);
        assertTrue(generateTypes.size() > 0);
    }
}