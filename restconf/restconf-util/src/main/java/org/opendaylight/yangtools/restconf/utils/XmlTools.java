/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import org.opendaylight.yangtools.restconf.client.api.dto.RestEventStreamInfo;
import org.opendaylight.yangtools.restconf.client.api.dto.RestModule;
import org.opendaylight.yangtools.restconf.client.api.dto.RestRpcService;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlTools {

    private static final Logger logger = LoggerFactory.getLogger(XmlTools.class.toString());
    static final String JAXP_SCHEMA_LOCATION =
            "http://java.sun.com/xml/jaxp/properties/schemaSource";

    public static Object unmarshallXml(Class<?> clazz,InputStream xmlStream,String namespace) throws Exception{
        if (null == xmlStream){
            throw new Exception("XML input stream can't be null");
        }
        JAXBContext jc = JAXBContext.newInstance(clazz);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        StreamSource xmlInputSource = new StreamSource(xmlStream);
        JAXBElement<?> obj = unmarshaller.unmarshal(xmlInputSource, clazz);
        return obj;
    }

    public static Document fromXml(InputStream is) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();
        return doc;
    }
    public static Set<RpcService> fromInputStream(InputStream is) throws Exception {
        Document doc = fromXml(is);

        return fromNodeList(doc.getElementsByTagName("play"));

    }
    public static Set<RpcService> fromNodeList(NodeList nodeList) throws Exception {
        Set<RpcService> rpcServices = new HashSet<RpcService>();
        for (int i =0; i < nodeList.getLength(); i++){
            org.w3c.dom.Node nNode = nodeList.item(i);
            if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                rpcServices.add(fromNode(nNode));
            }
        }
        return rpcServices;

    }
    public static RestRpcService fromNode(org.w3c.dom.Node node){
        Element eElement = (Element) node;
        RestRpcService rpcService = new RestRpcService(eElement.getAttribute("xmlns"));

        return rpcService;
    }
    private static EventStreamInfo restEventStreamInfoFromNode(org.w3c.dom.Node node) throws Exception {
        Element eElement = (Element) node;
        RestEventStreamInfo eventStreamInfo = new RestEventStreamInfo();
        eventStreamInfo.setDescription(eElement.getElementsByTagName("description").item(0).getTextContent());
        eventStreamInfo.setIdentifier(eElement.getElementsByTagName("identifier").item(0).getTextContent());
        if (null != eElement.getElementsByTagName("replay-log-creation-time")
                && eElement.getElementsByTagName("replay-log-creation-time").getLength()>0
                && !eElement.getElementsByTagName("replay-log-creation-time").item(0).getTextContent().equals("")){
            eventStreamInfo.setReplayLogCreationTime(DatatypeFactory
                    .newInstance()
                    .newXMLGregorianCalendar(eElement
                            .getElementsByTagName("replay-log-creation-time")
                            .item(0)
                            .getTextContent())
                    .toGregorianCalendar()
                    .getTime());
        }
        eventStreamInfo.setReplaySupported(Boolean.parseBoolean(eElement.getElementsByTagName("replay-support").item(0).getTextContent()));
        return eventStreamInfo;
    }

    public static Set<EventStreamInfo> evenStreamsFromInputStream(InputStream is) throws Exception {
        Document doc = fromXml(is);
        return streamInfoFromNodeList(doc.getElementsByTagName("stream"));
    }
    private static Set<EventStreamInfo> streamInfoFromNodeList(NodeList nodeList) throws Exception {
        Set<EventStreamInfo> rpcServices = new HashSet<EventStreamInfo>();
        for (int i =0; i < nodeList.getLength(); i++){
            org.w3c.dom.Node nNode = nodeList.item(i);
            if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                rpcServices.add(restEventStreamInfoFromNode(nNode));
            }
        }
        return rpcServices;
    }

    public static List<RestModule> getModulesFromInputStream(InputStream is) throws Exception {
        Document doc = fromXml(is);
        return restModulesFromNodeList(doc.getElementsByTagName("module"));
    }
    private static List<RestModule> restModulesFromNodeList(NodeList nodeList) throws Exception {
        List<RestModule> modules = new ArrayList<RestModule>();
        for (int i =0; i < nodeList.getLength(); i++){
            Node nNode = nodeList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE){
                modules.add(restModulefromNode(nNode));
            }
        }
        return modules;
    }
    private static RestModule restModulefromNode(Node node){
        Element eElement = (Element) node;
        RestModule restModule = new RestModule();
        try {
            restModule.setName(eElement.getElementsByTagName("name").item(0).getTextContent());
            restModule.setNamespace(eElement.getElementsByTagName("namespace").item(0).getTextContent());
            restModule.setRevision(eElement.getElementsByTagName("revision").item(0).getTextContent());
        } catch (NullPointerException npe){
            throw new IllegalStateException("Incomplete module data in xml.");
        }
        return restModule;
    }

}
