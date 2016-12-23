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

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.DeviateDefinition;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug7440Test {

    @Test
    public void testRestrictedTypeParentSchemaPathInDeviate() throws ReactorException, FileNotFoundException,
            URISyntaxException, ParseException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/bugs/bug7440/foo.yang");
        assertNotNull(schemaContext);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2016-12-23");

        final Module foo = schemaContext.findModuleByName("foo", revision);
        assertNotNull(foo);

        final Set<Deviation> deviations = foo.getDeviations();
        assertEquals(1, deviations.size());
        final Deviation deviation = deviations.iterator().next();

        final List<DeviateDefinition> deviates = deviation.getDeviates();
        assertEquals(1, deviates.size());
        final DeviateDefinition deviateReplace = deviates.iterator().next();

        final SchemaPath deviatedTypePath = SchemaPath.create(true, QName.create(foo.getQNameModule(), "test-leaf"),
                QName.create(foo.getQNameModule(), "uint32"));

        final TypeDefinition<?> deviatedType = deviateReplace.getDeviatedType();
        assertEquals(deviatedTypePath, deviatedType.getPath());
    }
}
