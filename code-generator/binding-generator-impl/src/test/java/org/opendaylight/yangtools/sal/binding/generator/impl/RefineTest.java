/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.*;
import static org.opendaylight.yangtools.sal.binding.generator.impl.SupportTestUtil.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

//Test for class RefineUtils
public class RefineTest {

    private static List<File> testModels = new ArrayList<>();

    private void loadTestResources() {
        final File listModelFile = new File(RefineTest.class.getResource("/refine.yang").getPath());
        testModels.add(listModelFile);
    }

    private void findUnknownNode(DataSchemaNode childNode, String unknownNodeValue, String unknownNodeName) {
        List<UnknownSchemaNode> unknownSchemaNodes = childNode.getUnknownSchemaNodes();
        boolean refinedUnknownNodeLflstFound = false;

        for (UnknownSchemaNode unknownSchemaNode : unknownSchemaNodes) {
            if (unknownSchemaNode.getNodeType().getLocalName().equals(unknownNodeName)
                    && unknownSchemaNode.getQName().getLocalName().equals(unknownNodeValue)) {
                refinedUnknownNodeLflstFound = true;
            }
        }
        assertTrue("Unknown node " + unknownNodeName + " with value " + unknownNodeValue + " wasn't found.",
                refinedUnknownNodeLflstFound);
    }

    private void findMustConstraint(ConstraintDefinition conDef, String mustValue) {
        boolean mustLflstFound = false;
        for (MustDefinition mustDef : conDef.getMustConstraints()) {
            if (mustDef.toString().equals(mustValue)) {
                mustLflstFound = true;
                break;
            }
        }
        assertTrue("Must element in 'lflst' is missing.", mustLflstFound);
    }

    @Test
    public void usesInGroupingDependenciesTest() {
        loadTestResources();
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);

        Module refineModule = null;
        for (Module module : modules) {
            if (module.getName().equals("module-refine")) {
                refineModule = module;
            }
        }
        assertNotNull("Refine module wasn't found.", refineModule);
        Set<DataSchemaNode> moduleChilds = refineModule.getChildNodes();
        DataSchemaNode lflstNode = null;
        DataSchemaNode chcNode = null;
        DataSchemaNode chc2Node = null;
        DataSchemaNode dataNode = null;
        for (DataSchemaNode childNode : moduleChilds) {
            if (childNode.getQName().getLocalName().equals("lflst")) {
                lflstNode = childNode;
            } else if (childNode.getQName().getLocalName().equals("chc")) {
                chcNode = childNode;
            } else if (childNode.getQName().getLocalName().equals("chc2")) {
                chc2Node = childNode;
            } else if (childNode.getQName().getLocalName().equals("data")) {
                dataNode = childNode;
            }
        }

        // lflst node
        assertNotNull("Node 'lflst' wasn't found.", lflstNode);
        ConstraintDefinition conDefLflst = lflstNode.getConstraints();
        assertEquals("Max elements number in 'lflst' is incorrect.", new Integer(64), conDefLflst.getMaxElements());
        assertEquals("Max elements number in 'lflst' is incorrect.", new Integer(32), conDefLflst.getMinElements());

        findMustConstraint(conDefLflst, "new = 57");

        boolean mustLflstFound = false;
        for (MustDefinition mustDef : conDefLflst.getMustConstraints()) {
            if (mustDef.toString().equals("new = 57")) {
                mustLflstFound = true;
                break;
            }
        }
        assertTrue("Must element in 'lflst' is missing.", mustLflstFound);

        findUnknownNode(lflstNode, "some value from lflst", "new-subnode");

        // chc node
        assertNotNull("Node 'chc' wasn't found.", chcNode);
        ChoiceNode choiceNode = null;
        if (chcNode instanceof ChoiceNode) {
            choiceNode = (ChoiceNode) chcNode;
        }
        assertNotNull("Choice node chc isn't of type ChoiceNode", choiceNode);
        assertEquals("chc node has incorrect default node.", "first", choiceNode.getDefaultCase());
        String unknownNodeChcValue = "some value from chc";
        String unknownNodeChcName = "new-subnode-chc";
        findUnknownNode(chcNode, unknownNodeChcValue, unknownNodeChcName);

        // chc2 node
        assertNotNull("Node 'chc2' wasn't found.", chc2Node);
        ConstraintDefinition conDefChc2 = chc2Node.getConstraints();
        assertFalse("'chc2' has incorrect value for 'mandatory'", conDefChc2.isMandatory());

        // data node
        assertNotNull("Node 'data' wasn't found.", dataNode);
        ConstraintDefinition conDefData = dataNode.getConstraints();
        assertFalse("'data' has incorrect value for 'mandatory'", conDefData.isMandatory());

        String unknownNodeDataValue = "some value from data";
        String unknownNodeDataName = "new-subnode-data";
        findUnknownNode(dataNode, unknownNodeDataValue, unknownNodeDataName);
        findMustConstraint(conDefData, "something-else = 9");
    }
}
