/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;

/**
 * The class <code>XmlTreeBuilderTest</code> contains tests for the class
 * {@link <code>XmlTreeBuilder</code>}
 *
 * @author Lukas Sedlak
 */
public class XmlTreeBuilderTest {

	private InputStream inputStream;
	
	/**
	 * Perform pre-test initialization
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		final String inputFile = getClass().getResource("/rpc-getDeviceEquipment.xml").getPath();
		inputStream = new FileInputStream(inputFile);
	}

	/**
	 * Run the Node<?> buildDataTree(InputStream) method test
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testBuildDataTree()
	{
		Node<?> rootNode = null;
		try {
			rootNode = XmlTreeBuilder.buildDataTree(inputStream);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		assertNotNull(rootNode);
		assertTrue(rootNode instanceof CompositeNode);
		
		CompositeNode compRootNode = (CompositeNode)rootNode;
		assertNotNull(compRootNode.getChildren());
		
		SimpleNode<String> methodName = null;
		SimpleNode<String> emptyTag = null;
		CompositeNode params = null;
		for (final Node<?> childNode : compRootNode.getChildren()) {
			if (childNode instanceof SimpleNode) {
				if ("emptyTag".equals(childNode.getNodeType().getLocalName())) {
					emptyTag = (SimpleNode<String>) childNode;
				} else if ("methodName".equals(childNode.getNodeType().getLocalName())) {
					methodName = (SimpleNode<String>) childNode;
				}
				
			} else if (childNode instanceof CompositeNode) {
				params = (CompositeNode) childNode;
			}
		}
		
		assertNotNull(methodName);
		assertNotNull(params);
		assertTrue(emptyTag.getValue().isEmpty());
		assertEquals(methodName.getValue(), "getDeviceEquipment");
		
		String deviceId = null;
		String deviceIP = null;
		for (final Node<?> param : params.getChildren()) {
			if (param instanceof CompositeNode) {
				final Node<?> valueNode = ((CompositeNode) param).getChildren().get(0);
				
				assertTrue(valueNode instanceof CompositeNode);
				final CompositeNode value = (CompositeNode) valueNode;
				final Node<?> stringNode = value.getChildren().get(0);
				assertTrue(stringNode instanceof SimpleNode);
				
				final SimpleNode<String> string = (SimpleNode<String>) stringNode;
				if ("DeviceID123".equals(string.getValue())) {
					deviceId = string.getValue();
				} else if ("172.23.218.75".equals(string.getValue())) {
					deviceIP = string.getValue();
				}
			}
		}
		
		assertNotNull(deviceId);
		assertNotNull(deviceIP);
	}
	
	@Test
	public void nodeMapInCompositeNodeTest() {
	    Node<?> rootNode = null;
            try {
                    rootNode = XmlTreeBuilder.buildDataTree(inputStream);
            } catch (XMLStreamException e) {
                    e.printStackTrace();
            }
            
            CompositeNode compRootNode = (CompositeNode)rootNode;
            List<CompositeNode> params = compRootNode.getCompositesByName("params");
            assertEquals(1, params.size());
            List<CompositeNode> compositesByName = params.get(0).getCompositesByName("param");
            assertEquals(2, compositesByName.size());
	}
}