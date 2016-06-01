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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug5712Test {

    @Test
    public void testTypedefWithNewStatementParser() throws ReactorException, SourceException, FileNotFoundException,
            URISyntaxException {
        SchemaContext schemaContext = StmtTestUtils.parseYangSources("/bugs/bug5712");
        assertNotNull(schemaContext);

        Module badModule = schemaContext.findModuleByName("bad", null);
        assertNotNull(badModule);

        checkThing2TypeDef(badModule);
    }

    private static void checkThing2TypeDef(final Module badModule) {
        TypeDefinition<?> thing2 = null;
        for (TypeDefinition<?> typeDef : badModule.getTypeDefinitions()) {
            if (typeDef.getQName().getLocalName().equals("thing2")) {
                thing2 = typeDef;
                break;
            }
        }

        assertNotNull(thing2);
        TypeDefinition<?> baseType = thing2.getBaseType();
        assertEquals(QName.create("urn:opendaylight:bad", "2016-04-11", "thing"), baseType.getQName());
    }

    @Test
    public void testTypedefWithOldParser() throws URISyntaxException, IOException {
        Set<Module> modules = loadModules(getClass().getResource("/bugs/bug5712").toURI());
        assertNotNull(modules);
        assertEquals(1, modules.size());

        checkThing2TypeDef(modules.iterator().next());
    }

    public static Set<Module> loadModules(final URI resourceDirectory) throws IOException {
        return loadSchemaContext(resourceDirectory).getModules();
    }

    public static SchemaContext loadSchemaContext(final URI resourceDirectory) throws IOException {
        final YangContextParser parser = new YangParserImpl();
        final File testDir = new File(resourceDirectory);
        final String[] fileList = testDir.list();
        final List<File> testFiles = new ArrayList<>();
        if (fileList == null) {
            throw new FileNotFoundException(resourceDirectory.toString());
        }
        for (String fileName : fileList) {
            testFiles.add(new File(testDir, fileName));
        }
        return parser.parseFiles(testFiles);
    }

}
