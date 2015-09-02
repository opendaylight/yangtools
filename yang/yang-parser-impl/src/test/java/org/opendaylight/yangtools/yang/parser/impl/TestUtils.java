/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertEquals;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils;
import org.opendaylight.yangtools.yang.parser.util.NamedByteArrayInputStream;

final class TestUtils {

    private TestUtils() {
    }

    public static SchemaContext loadSchemaContext(final URI resourceDirectory)
            throws IOException {
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

    public static Set<Module> loadModules(final URI resourceDirectory)
            throws IOException {
        return loadSchemaContext(resourceDirectory).getModules();
    }

    public static Set<Module> loadModules(final List<InputStream> input) throws IOException, YangSyntaxErrorException {
        Collection<ByteSource> sources = BuilderUtils.streamsToByteSources(input);
        final YangContextParser parser = new YangParserImpl();
        SchemaContext ctx = parser.parseSources(sources);
        return ctx.getModules();
    }

    public static Module loadModule(final InputStream stream) throws IOException, YangSyntaxErrorException {
        final YangContextParser parser = new YangParserImpl();
        ByteSource source = new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return NamedByteArrayInputStream.create(stream);
            }
        };
        final Collection<ByteSource> sources = Collections.singletonList(source);
        SchemaContext ctx = parser.parseSources(sources);
        return ctx.getModules().iterator().next();
    }

    public static Module loadModuleWithContext(final String name, final InputStream stream, final SchemaContext context)
            throws IOException, YangSyntaxErrorException {
        final YangContextParser parser = new YangParserImpl();

        final byte[] streamContent = ByteStreams.toByteArray(stream);

        ByteSource source = new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return new ByteArrayInputStream(streamContent);
            }
        };

        final Collection<ByteSource> sources = Collections.singletonList(source);
        SchemaContext ctx = parser.parseSources(sources, context);
        final Set<Module> modules = ctx.getModules();
        stream.close();
        Module result = null;
        for (Module module : modules) {
            if (module.getName().equals(name)) {
                result = module;
                break;
            }
        }
        return result;
    }

    public static Set<Module> loadModulesWithContext(final Collection<InputStream> input, final SchemaContext context)
            throws IOException, YangSyntaxErrorException {
        Collection<ByteSource> sources = BuilderUtils.streamsToByteSources(input);
        final YangContextParser parser = new YangParserImpl();
        SchemaContext ctx = parser.parseSources(sources, context);
        final Set<Module> modules = ctx.getModules();
        return modules;
    }

    public static Module findModule(final Set<Module> modules, final String moduleName) {
        Module result = null;
        for (Module module : modules) {
            if (module.getName().equals(moduleName)) {
                result = module;
                break;
            }
        }
        return result;
    }

    public static ModuleImport findImport(final Set<ModuleImport> imports, final String prefix) {
        ModuleImport result = null;
        for (ModuleImport moduleImport : imports) {
            if (moduleImport.getPrefix().equals(prefix)) {
                result = moduleImport;
                break;
            }
        }
        return result;
    }

    public static TypeDefinition<?> findTypedef(final Set<TypeDefinition<?>> typedefs, final String name) {
        TypeDefinition<?> result = null;
        for (TypeDefinition<?> td : typedefs) {
            if (td.getQName().getLocalName().equals(name)) {
                result = td;
                break;
            }
        }
        return result;
    }

    public static SchemaPath createPath(final boolean absolute, final URI namespace, final Date revision, final String prefix, final String... names) {
        List<QName> path = new ArrayList<>();
        for (String name : names) {
            path.add(QName.create(namespace, revision, name));
        }
        return SchemaPath.create(path, absolute);
    }

    public static Date createDate(final String date) {
        Date result;
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            result = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            result = null;
        }
        return result;
    }

    /**
     * Test if node has augmenting flag set to expected value. In case this is
     * DataNodeContainer/ChoiceNode, check its child nodes/case nodes too.
     *
     * @param node
     *            node to check
     * @param expected
     *            expected value
     */
    public static void checkIsAugmenting(final DataSchemaNode node, final boolean expected) {
        assertEquals(expected, node.isAugmenting());
        if (node instanceof DataNodeContainer) {
            for (DataSchemaNode child : ((DataNodeContainer) node).getChildNodes()) {
                checkIsAugmenting(child, expected);
            }
        } else if (node instanceof ChoiceSchemaNode) {
            for (ChoiceCaseNode caseNode : ((ChoiceSchemaNode) node).getCases()) {
                checkIsAugmenting(caseNode, expected);
            }
        }
    }

    /**
     * Check if node has addedByUses flag set to expected value. In case this is
     * DataNodeContainer/ChoiceNode, check its child nodes/case nodes too.
     *
     * @param node
     *            node to check
     * @param expected
     *            expected value
     */
    public static void checkIsAddedByUses(final DataSchemaNode node, final boolean expected) {
        assertEquals(expected, node.isAddedByUses());
        if (node instanceof DataNodeContainer) {
            for (DataSchemaNode child : ((DataNodeContainer) node).getChildNodes()) {
                checkIsAddedByUses(child, expected);
            }
        } else if (node instanceof ChoiceSchemaNode) {
            for (ChoiceCaseNode caseNode : ((ChoiceSchemaNode) node).getCases()) {
                checkIsAddedByUses(caseNode, expected);
            }
        }
    }

    public static void checkIsAddedByUses(final GroupingDefinition node, final boolean expected) {
        assertEquals(expected, node.isAddedByUses());
        for (DataSchemaNode child : ((DataNodeContainer) node).getChildNodes()) {
            checkIsAddedByUses(child, expected);
        }
    }

    public static List<Module> findModules(final Set<Module> modules, final String moduleName) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getName().equals(moduleName)) {
                result.add(module);
            }
        }
        return result;
    }

}
