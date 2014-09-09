/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.utils;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.impl.ImmutableCompositeNode;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.opendaylight.yangtools.yang.data.impl.codec.DeserializationException;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class RestconfUtils {

    private static final Logger logger = LoggerFactory.getLogger(RestconfUtils.class);

    private static final BiMap<URI, String> uriToModuleName = HashBiMap.<URI, String> create();

    public static Entry<String, DataSchemaNode> toRestconfIdentifier(
            final org.opendaylight.yangtools.yang.binding.InstanceIdentifier<?> bindingIdentifier,
            final BindingIndependentMappingService mappingService, final SchemaContext schemaContext) {
        YangInstanceIdentifier domIdentifier = mappingService.toDataDom(bindingIdentifier);
        return toRestconfIdentifier(domIdentifier, schemaContext);
    }

    public static Entry<String, DataSchemaNode> toRestconfIdentifier(final YangInstanceIdentifier xmlInstanceIdentifier,
            final SchemaContext schemaContext) {

        final Iterable<YangInstanceIdentifier.PathArgument> elements = xmlInstanceIdentifier.getPathArguments();
        final StringBuilder ret = new StringBuilder();
        final QName startQName = elements.iterator().next().getNodeType();
        URI namespace = startQName.getNamespace();
        Date revision = startQName.getRevision();
        final Module initialModule = schemaContext.findModuleByNamespaceAndRevision(namespace, revision);
        DataNodeContainer node = (initialModule);
        DataSchemaNode schemaNode = null;
        for (final YangInstanceIdentifier.PathArgument element : elements) {
            final DataSchemaNode potentialNode = node.getDataChildByName(element.getNodeType());
            if (!isListOrContainer(potentialNode)) {
                return null;
            }
            node = ((DataNodeContainer) potentialNode);
            schemaNode = potentialNode;
            ret.append(convertToRestconfIdentifier(element, node, schemaContext));
        }
        return new SimpleEntry<>(ret.toString(), schemaNode);
    }

    private static CharSequence convertContainerToRestconfIdentifier(final YangInstanceIdentifier.NodeIdentifier argument,
            final SchemaContext schemaContext) {
        return "/" + toRestconfIdentifier(argument.getNodeType(), schemaContext);
    }

    private static CharSequence convertListToRestconfIdentifier(
            final YangInstanceIdentifier.NodeIdentifierWithPredicates argument, final ListSchemaNode node,
            final SchemaContext schemaContext) {
        QName _nodeType = argument.getNodeType();
        final CharSequence nodeIdentifier = toRestconfIdentifier(_nodeType, schemaContext);
        final Map<QName, Object> keyValues = argument.getKeyValues();

        StringBuilder sb = new StringBuilder();
        sb.append('/');
        sb.append(nodeIdentifier);
        sb.append('/');

        List<QName> keyDefinition = node.getKeyDefinition();
        boolean _hasElements = false;
        for (final QName key : keyDefinition) {
            if (_hasElements) {
                sb.append('/');
            } else {
                _hasElements = true;
            }
            Object _get = keyValues.get(key);
            sb.append(toUriString(_get));
        }

        return sb.toString();
    }

    private static String toUriString(final Object object) {
        if (object == null) {
            return "";
        }
        String _string = object.toString();
        return URLEncoder.encode(_string);
    }

    public static CharSequence toRestconfIdentifier(final QName qname, final SchemaContext schemaContext) {
        URI namespace = qname.getNamespace();
        String module = uriToModuleName.get(namespace);
        if (module == null) {
            Date revision = qname.getRevision();
            final Module moduleSchema = schemaContext.findModuleByNamespaceAndRevision(namespace, revision);
            if (moduleSchema == null) {
                return null;
            }
            String name = moduleSchema.getName();
            uriToModuleName.put(namespace, name);
            module = name;
        }
        return module + ':' + qname.getLocalName();
    }

    private static CharSequence convertToRestconfIdentifier(final YangInstanceIdentifier.PathArgument argument,
            final DataNodeContainer node, final SchemaContext schemaContext) {
        if (argument instanceof YangInstanceIdentifier.NodeIdentifier) {
            return convertContainerToRestconfIdentifier((NodeIdentifier) argument, schemaContext);
        } else if (argument instanceof YangInstanceIdentifier.NodeIdentifierWithPredicates
                && node instanceof ListSchemaNode) {
            return convertListToRestconfIdentifier((NodeIdentifierWithPredicates) argument, (ListSchemaNode) node,
                    schemaContext);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " + Arrays.asList(argument, node).toString());
        }
    }

    private static boolean isListOrContainer(final DataSchemaNode node) {
        if (node instanceof ListSchemaNode || node instanceof ContainerSchemaNode) {
            return true;
        }
        return false;
    }

    public static Module findModuleByNamespace(final URI namespace, final SchemaContext schemaContext) {
        Preconditions.checkArgument(namespace != null);
        final Set<Module> moduleSchemas = schemaContext.findModuleByNamespace(namespace);
        Module filterLatestModule = null;
        if (moduleSchemas != null) {
            filterLatestModule = filterLatestModule(moduleSchemas);
        }
        return filterLatestModule;
    }

    private static Module filterLatestModule(final Iterable<Module> modules) {
        Module latestModule = Iterables.getFirst(modules, null);
        for (final Module module : modules) {
            Date revision = module.getRevision();
            Date latestModuleRevision = latestModule.getRevision();
            if (revision.after(latestModuleRevision)) {
                latestModule = module;
            }
        }
        return latestModule;
    }

    public String findModuleNameByNamespace(final URI namespace, final SchemaContext schemaContext) {
        String moduleName = uriToModuleName.get(namespace);
        if (moduleName == null) {
            final Module module = findModuleByNamespace(namespace, schemaContext);
            if (module == null) {
                return null;
            }
            moduleName = module.getName();
            uriToModuleName.put(namespace, moduleName);
        }
        return moduleName;
    }

    /**
     * Parse rpc services from input stream.
     *
     * @param inputStream
     *            stream containing rpc services definition in xml
     *            representation
     * @param mappingService
     *            current mapping service
     * @param schemaContext
     *            parsed yang data context
     * @return Set of classes representing rpc services parsed from input stream
     */
    public static Set<Class<? extends RpcService>> rpcServicesFromInputStream(final InputStream inputStream,
            final BindingIndependentMappingService mappingService, final SchemaContext schemaContext) {
        try {
            DocumentBuilderFactory documentBuilder = DocumentBuilderFactory.newInstance();
            documentBuilder.setNamespaceAware(true);
            DocumentBuilder builder = documentBuilder.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            Element rootElement = doc.getDocumentElement();

            List<Node<?>> domNodes = XmlDocumentUtils.toDomNodes(rootElement,
                    Optional.of(schemaContext.getChildNodes()));
            Set<Class<? extends RpcService>> rpcServices = new HashSet<Class<? extends RpcService>>();
            for (Node<?> node : domNodes) {
                if (node instanceof ImmutableCompositeNode) {
                    ImmutableCompositeNode icNode = (ImmutableCompositeNode) node;
                    QName namespace = null;
                    QName revision = null;
                    QName name = null;
                    for (QName q : icNode.keySet()) {
                        if (q.getLocalName().equals("namespace")) {
                            namespace = q;
                        }
                        if (q.getLocalName().equals("revision")) {
                            revision = q;
                        }
                        if (q.getLocalName().equals("name")) {
                            name = q;
                        }

                    }

                    // FIXME: Method getRpcServiceClassFor has been modified and
                    // fixed to follow API contract. This call MUST be updated
                    // to follow contract i.e. pass correct parameters:
                    // "NAMESPACE" and "REVISION"
                    Optional<Class<? extends RpcService>> rpcService = mappingService.getRpcServiceClassFor(
                            icNode.get(name).get(0).getValue().toString(), icNode.get(revision).get(0).getValue()
                                    .toString());
                    if (rpcService.isPresent()) {
                        rpcServices.add(rpcService.get());
                    }
                }
            }

            return rpcServices;
        } catch (ParserConfigurationException e) {
            logger.trace("Parse configuration exception {}", e);
        } catch (SAXException e) {
            logger.trace("SAX exception {}", e);
        } catch (IOException e) {
            logger.trace("IOException {}", e);
        }
        return null;
    }

    /**
     * Parse DataObject from input stream.
     *
     * @param path
     *            identifier of expected result object
     * @param inputStream
     *            stream containing xml data to parse
     * @param schemaContext
     *            parsed yang data context
     * @param mappingService
     *            current mapping service
     * @param dataSchema
     *            yang data schema node representation of resulting data object
     * @return DataObject instance parsed from input stream
     */
    public static DataObject dataObjectFromInputStream(
            final org.opendaylight.yangtools.yang.binding.InstanceIdentifier<?> path, final InputStream inputStream,
            final SchemaContext schemaContext, final BindingIndependentMappingService mappingService,
            final DataSchemaNode dataSchema) {
        // Parse stream into w3c Document
        try {
            DocumentBuilderFactory documentBuilder = DocumentBuilderFactory.newInstance();
            documentBuilder.setNamespaceAware(true);
            DocumentBuilder builder = documentBuilder.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            Element rootElement = doc.getDocumentElement();
            Node<?> domNode = XmlDocumentUtils.toDomNode(rootElement, Optional.of(dataSchema),
                    Optional.<XmlCodecProvider> absent(), Optional.of(schemaContext));
            DataObject dataObject = mappingService.dataObjectFromDataDom(path, (CompositeNode) domNode); // getDataFromResponse
            return dataObject;
        } catch (DeserializationException e) {
            logger.trace("Deserialization exception {}", e);
        } catch (ParserConfigurationException e) {
            logger.trace("Parse configuration exception {}", e);
        } catch (SAXException e) {
            logger.trace("SAX exception {}", e);
        } catch (IOException e) {
            logger.trace("IOException {}", e);
        }
        return null;
    }

}
