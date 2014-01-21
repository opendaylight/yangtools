/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;

final class TestUtils {

    private TestUtils() {
    }

    public static Set<Module> loadModules(String resourceDirectory) throws FileNotFoundException {
        YangModelParser parser = new YangParserImpl();
        return loadModules(resourceDirectory, parser);
    }

    public static Set<Module> loadModules(String resourceDirectory, YangModelParser parser) throws FileNotFoundException {
        final File testDir = new File(resourceDirectory);
        final String[] fileList = testDir.list();
        final List<File> testFiles = new ArrayList<>();
        if (fileList == null) {
            throw new FileNotFoundException(resourceDirectory);
        }
        for (String fileName : fileList) {
            testFiles.add(new File(testDir, fileName));
        }
        return parser.parseYangModels(testFiles);
    }

    public static Set<Module> loadModules(List<InputStream> input) throws IOException {
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = new HashSet<>(parser.parseYangModelsFromStreams(input));
        for (InputStream stream : input) {
            stream.close();
        }
        return modules;
    }

    public static Module loadModule(final InputStream stream) throws IOException {
        final YangModelParser parser = new YangParserImpl();
        final List<InputStream> input = Collections.singletonList(stream);
        final Set<Module> modules = new HashSet<>(parser.parseYangModelsFromStreams(input));
        stream.close();
        return modules.iterator().next();
    }

    public static Module loadModuleWithContext(final String name, final InputStream stream, final SchemaContext context)
            throws IOException {
        final YangModelParser parser = new YangParserImpl();
        final List<InputStream> input = Collections.singletonList(stream);
        final Set<Module> modules = new HashSet<>(parser.parseYangModelsFromStreams(input, context));
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

    public static Set<Module> loadModulesWithContext(final List<InputStream> input, final SchemaContext context)
            throws IOException {
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = new HashSet<>(parser.parseYangModelsFromStreams(input, context));
        for (InputStream is : input) {
            if (is != null) {
                is.close();
            }
        }
        return modules;
    }

    public static Module findModule(Set<Module> modules, String moduleName) {
        Module result = null;
        for (Module module : modules) {
            if (module.getName().equals(moduleName)) {
                result = module;
                break;
            }
        }
        return result;
    }

    public static ModuleImport findImport(Set<ModuleImport> imports, String prefix) {
        ModuleImport result = null;
        for (ModuleImport moduleImport : imports) {
            if (moduleImport.getPrefix().equals(prefix)) {
                result = moduleImport;
                break;
            }
        }
        return result;
    }

    public static TypeDefinition<?> findTypedef(Set<TypeDefinition<?>> typedefs, String name) {
        TypeDefinition<?> result = null;
        for (TypeDefinition<?> td : typedefs) {
            if (td.getQName().getLocalName().equals(name)) {
                result = td;
                break;
            }
        }
        return result;
    }

    public static SchemaPath createPath(boolean absolute, URI namespace, Date revision, String prefix, String... names) {
        List<QName> path = new ArrayList<>();
        for (String name : names) {
            path.add(new QName(namespace, revision, prefix, name));
        }
        return new SchemaPath(path, absolute);
    }

    public static Date createDate(String date) {
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
    public static void checkIsAugmenting(DataSchemaNode node, boolean expected) {
        assertEquals(expected, node.isAugmenting());
        if (node instanceof DataNodeContainer) {
            for (DataSchemaNode child : ((DataNodeContainer) node).getChildNodes()) {
                checkIsAugmenting(child, expected);
            }
        } else if (node instanceof ChoiceNode) {
            for (ChoiceCaseNode caseNode : ((ChoiceNode) node).getCases()) {
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
    public static void checkIsAddedByUses(DataSchemaNode node, boolean expected) {
        assertEquals(expected, node.isAddedByUses());
        if (node instanceof DataNodeContainer) {
            for (DataSchemaNode child : ((DataNodeContainer) node).getChildNodes()) {
                checkIsAddedByUses(child, expected);
            }
        } else if (node instanceof ChoiceNode) {
            for (ChoiceCaseNode caseNode : ((ChoiceNode) node).getCases()) {
                checkIsAddedByUses(caseNode, expected);
            }
        }
    }

    public static void checkIsAddedByUses(GroupingDefinition node, boolean expected) {
        assertEquals(expected, node.isAddedByUses());
        if (node instanceof DataNodeContainer) {
            for (DataSchemaNode child : ((DataNodeContainer) node).getChildNodes()) {
                checkIsAddedByUses(child, expected);
            }
        } else if (node instanceof ChoiceNode) {
            for (ChoiceCaseNode caseNode : ((ChoiceNode) node).getCases()) {
                checkIsAddedByUses(caseNode, expected);
            }
        }
    }

    public static List<Module> findModules(Set<Module> modules, String moduleName) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getName().equals(moduleName)) {
                result.add(module);
            }
        }
        return result;
    }

}
