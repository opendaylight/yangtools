/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.AnyXmlBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ConstraintsBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.opendaylight.yangtools.yang.parser.util.RefineHolder;
import org.opendaylight.yangtools.yang.parser.util.RefineUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

//Test for class RefineUtils
public class RefineTest {

    private static List<File> testModels = new ArrayList<>();

    private void loadTestResources() throws URISyntaxException {
        final File listModelFile = new File(RefineTest.class.getResource("/refine.yang").toURI());
        testModels.add(listModelFile);
    }

    private void findUnknownNode(final DataSchemaNodeBuilder childNode, final String unknownNodeValue, final String unknownNodeName) {
        List<UnknownSchemaNodeBuilder> unknownSchemaNodesBuilder = childNode.getUnknownNodes();
        boolean refinedUnknownNodeLflstFound = false;

        for (UnknownSchemaNodeBuilder unknownSchemaNodeBuilders : unknownSchemaNodesBuilder) {
            if (unknownSchemaNodeBuilders.getNodeType().getLocalName().equals(unknownNodeName)
                    && unknownSchemaNodeBuilders.getQName().getLocalName().equals(unknownNodeValue)) {
                refinedUnknownNodeLflstFound = true;
            }
        }
        assertTrue("Unknown node " + unknownNodeName + " with value " + unknownNodeValue + " wasn't found.",
                refinedUnknownNodeLflstFound);
    }

    private void findMustConstraint(final ConstraintsBuilder conDef, final String mustValue) {
        boolean mustLflstFound = false;
        for (MustDefinition mustDef : conDef.getMustDefinitions()) {
            if (mustDef.toString().equals(mustValue)) {
                mustLflstFound = true;
                break;
            }
        }
        assertTrue("Must element in 'lflst' is missing.", mustLflstFound);
    }

    // FIXME: rework test
    @Ignore
    @Test
    public void usesInGroupingDependenciesTest() throws URISyntaxException {
        loadTestResources();
        assertEquals("Incorrect number of test files.", 1, testModels.size());

        Set<UsesNodeBuilder> usesNodeBuilders = getModuleBuilder().getUsesNodeBuilders();
        List<RefineHolder> refineHolders = null;
        Set<DataSchemaNodeBuilder> dataSchemaNodeBuilders = null;
        for (UsesNodeBuilder usesNodeBuilder : usesNodeBuilders) {
            if (usesNodeBuilder.getGroupingPathAsString().equals("grp")) {
                refineHolders = usesNodeBuilder.getRefines();
                dataSchemaNodeBuilders = usesNodeBuilder.getParent().getChildNodeBuilders();
                break;
            }
        }

        assertNotNull("List of refine holders wasn't initialized.", refineHolders);
        assertEquals("Incorrect number of refine holders", 4, refineHolders.size());

        checkLflstRefineHolderAndSchemaNodeBuilder("lflst", refineHolders, dataSchemaNodeBuilders);
        checkChcRefineHolderAndSchemaNodeBuilder("chc", refineHolders, dataSchemaNodeBuilders);
        checkChc2RefineHolderAndSchemaNodeBuilder("chc2", refineHolders, dataSchemaNodeBuilders);
        checkAnyXmlRefineHolderAndSchemaNodeBuilder("data", refineHolders, dataSchemaNodeBuilders);
    }

    private ModuleBuilder getModuleBuilder() {
        Class<YangParserImpl> cl = YangParserImpl.class;

        YangParserImpl yangParserImpl = null;
        yangParserImpl = new YangParserImpl();
        assertNotNull("Instance of YangParserImpl isn't created", yangParserImpl);

        Method methodResolveModuleBuilders = null;
        try {
            methodResolveModuleBuilders = cl.getDeclaredMethod("resolveModuleBuilders", java.util.Collection.class, SchemaContext.class);
        } catch (NoSuchMethodException | SecurityException e1) {
        }
        assertNotNull("The method resolveModuleBuilders cannot be found", methodResolveModuleBuilders);

        final Map<InputStream, File> inputStreams = Maps.newHashMap();

        for (final File yangFile : testModels) {
            try {
                inputStreams.put(new FileInputStream(yangFile), yangFile);
            } catch (FileNotFoundException e) {
            }
        }
        assertEquals("Map with input streams contains incorrect number of files.", 1, inputStreams.size());

        Map<ModuleBuilder, InputStream> builderToStreamMap = Maps.newHashMap();
        Map<String, Map<Date, ModuleBuilder>> modules = null;
        try {
            methodResolveModuleBuilders.setAccessible(true);
            modules = (Map<String, Map<Date, ModuleBuilder>>) methodResolveModuleBuilders.invoke(yangParserImpl,
                    Lists.newArrayList(inputStreams.keySet()), null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        assertEquals("Map with modules contains incorrect number of modules", 1, modules.size());
        Map<Date, ModuleBuilder> mapWithModuleBuilder = modules.get("module-refine");
        assertEquals("Map with module builders contains incorrect number of modules", 1, mapWithModuleBuilder.size());
        Date date = new GregorianCalendar(2013, GregorianCalendar.SEPTEMBER, 11).getTime();
        ModuleBuilder moduleBuilder = mapWithModuleBuilder.get(date);
        assertNotNull("Module builder wasn't find", moduleBuilder);
        return moduleBuilder;
    }

    private void checkAnyXmlRefineHolderAndSchemaNodeBuilder(final String string, final List<RefineHolder> refineHolders,
            final Set<DataSchemaNodeBuilder> dataSchemaNodeBuilders) {
        RefineHolder refHolderData = getRefineHolder("data", refineHolders);

        QName qname = createQname();
        DataSchemaNodeBuilder builderData = new AnyXmlBuilder("module", 4, qname, createSchemaPath(qname));

        assertNotNull("Refine holder data wasn't initialized.", refHolderData);
        RefineUtils.refineAnyxml((AnyXmlBuilder) builderData, refHolderData);

        // data node
        ConstraintsBuilder conDefData = builderData.getConstraints();
        assertFalse("'data' has incorrect value for 'mandatory'", conDefData.isMandatory());

        String unknownNodeDataValue = "some value from data";
        String unknownNodeDataName = "new-subnode-data";
        findUnknownNode(builderData, unknownNodeDataValue, unknownNodeDataName);
        findMustConstraint(conDefData, "something-else = 9");

    }

    private void checkChc2RefineHolderAndSchemaNodeBuilder(final String nodeName, final List<RefineHolder> refineHolders,
            final Set<DataSchemaNodeBuilder> dataSchemaNodeBuilders) {
        RefineHolder refHolderChc2 = getRefineHolder("chc2", refineHolders);

        QName qname = createQname();
        List<QName> path = Lists.newArrayList(qname);
        DataSchemaNodeBuilder builderChc2 = new ChoiceBuilder("module", 4, qname, SchemaPath.create(path, true));
        assertNotNull("Refine holder chc2 wasn't initialized.", refHolderChc2);

        RefineUtils.refineChoice((ChoiceBuilder) builderChc2, refHolderChc2);

        // chc2 node
        ConstraintsBuilder conDefChc2 = builderChc2.getConstraints();
        assertFalse("'chc2' has incorrect value for 'mandatory'", conDefChc2.isMandatory());
    }

    private void checkChcRefineHolderAndSchemaNodeBuilder(final String nodeName, final List<RefineHolder> refineHolders,
            final Set<DataSchemaNodeBuilder> dataSchemaNodeBuilders) {
        RefineHolder refHolderChc = getRefineHolder("chc", refineHolders);

        QName qname = createQname();
        List<QName> path = Lists.newArrayList(qname);
        DataSchemaNodeBuilder builderChc = new ChoiceBuilder("module", 4, qname, SchemaPath.create(path, true));

        assertNotNull("Refine holder chc wasn't initialized.", refHolderChc);
        assertNotNull("Data schema node builder chc wasn't initialized.", builderChc);
        RefineUtils.refineChoice((ChoiceBuilder) builderChc, refHolderChc);

        ChoiceBuilder choiceBuilder = null;
        if (builderChc instanceof ChoiceBuilder) {
            choiceBuilder = (ChoiceBuilder) builderChc;
        }
        assertNotNull("Choice node chc isn't of type ChoiceBuilder", choiceBuilder);
        assertEquals("chc node has incorrect default node.", "first", choiceBuilder.getDefaultCase());
        String unknownNodeChcValue = "some value from chc";
        String unknownNodeChcName = "new-subnode-chc";
        findUnknownNode(choiceBuilder, unknownNodeChcValue, unknownNodeChcName);
    }

    private void checkLflstRefineHolderAndSchemaNodeBuilder(final String nodeName, final List<RefineHolder> refineHolders,
            final Set<DataSchemaNodeBuilder> dataSchemaNodeBuilders) {
        RefineHolder refHolderLflst = getRefineHolder(nodeName, refineHolders);

        QName qname = createQname();
        DataSchemaNodeBuilder builderLflst = new LeafListSchemaNodeBuilder("module", 4, qname, createSchemaPath(qname));

        assertNotNull("Refine holder " + nodeName + " wasn't initialized.", refHolderLflst);
        assertNotNull("Data schema node builder " + nodeName + " wasn't initialized.", builderLflst);
        RefineUtils.refineLeafList((LeafListSchemaNodeBuilder) builderLflst, refHolderLflst);
        // lflst node

        ConstraintsBuilder conDefLflst = builderLflst.getConstraints();
        assertEquals("Max elements number in " + nodeName + " is incorrect.", new Integer(64),
                conDefLflst.getMaxElements());
        assertEquals("Max elements number in " + nodeName + " is incorrect.", new Integer(32),
                conDefLflst.getMinElements());

        findMustConstraint(conDefLflst, "new = 57");

        boolean mustLflstFound = false;
        for (MustDefinition mustDef : conDefLflst.getMustDefinitions()) {
            if (mustDef.toString().equals("new = 57")) {
                mustLflstFound = true;
                break;
            }
        }
        assertTrue("Must element in " + nodeName + " is missing.", mustLflstFound);

        findUnknownNode(builderLflst, "some value from " + nodeName, "new-subnode");

    }

    private RefineHolder getRefineHolder(final String refHolderName, final List<RefineHolder> refineHolders) {
        for (RefineHolder refineHolder : refineHolders) {
            if (refineHolder.getName().equals(refHolderName)) {
                return refineHolder;
            }
        }
        return null;
    }

    private QName createQname() {
        QName qname = null;
        boolean uriCreated = false;
        try {
            qname = new QName(new URI("uri"), "q name");
            uriCreated = true;
        } catch (URISyntaxException e) {
        }
        assertTrue("Qname wasn't created sucessfully.", uriCreated);
        return qname;
    }

    private SchemaPath createSchemaPath(final QName qname) {
        List<QName> qnames = new ArrayList<>();
        qnames.add(createQname());
        return SchemaPath.create(qnames, true);
    }

}
