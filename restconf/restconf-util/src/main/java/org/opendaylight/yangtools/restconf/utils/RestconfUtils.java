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
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.Node;
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

    private static final BiMap<URI,String> uriToModuleName = new Functions.Function0<BiMap<URI,String>>() {
        @Override
        public BiMap<URI,String> apply() {
            HashBiMap<URI,String> _create = HashBiMap.<URI, String>create();
            return _create;
        }
    }.apply();


    public static Entry<String,DataSchemaNode> toRestconfIdentifier(org.opendaylight.yangtools.yang.binding.InstanceIdentifier<?> bindingIdentifier, BindingIndependentMappingService mappingService, SchemaContext schemaContext) {
        InstanceIdentifier domIdentifier = mappingService.toDataDom(bindingIdentifier);
        return toRestconfIdentifier(domIdentifier, schemaContext);

    }

    public static Entry<String,DataSchemaNode> toRestconfIdentifier(
            InstanceIdentifier xmlInstanceIdentifier,
            SchemaContext schemaContext) {

        final List<InstanceIdentifier.PathArgument> elements = xmlInstanceIdentifier.getPath();
        final StringBuilder ret = new StringBuilder();
        InstanceIdentifier.PathArgument _head = IterableExtensions.<InstanceIdentifier.PathArgument>head(elements);
        final QName startQName = _head.getNodeType();
        URI _namespace = startQName.getNamespace();
        Date _revision = startQName.getRevision();
        final Module initialModule = schemaContext.findModuleByNamespaceAndRevision(_namespace, _revision);
        DataNodeContainer node = (initialModule);
        DataSchemaNode schemaNode = null;
        for (final InstanceIdentifier.PathArgument element : elements) {
            {
                final DataSchemaNode potentialNode = schemaContext.getDataChildByName(element.getNodeType());
                if (!isListOrContainer(potentialNode)) {
                    return null;
                }
                node = ((DataNodeContainer) potentialNode);
                schemaNode = potentialNode;
                ret.append(convertToRestconfIdentifier(element, node,schemaContext));
            }
        }
        return new SimpleEntry<>(ret.toString(),schemaNode);
    }

    private static CharSequence convertContainerToRestconfIdentifier(final InstanceIdentifier.NodeIdentifier argument, final ContainerSchemaNode node, SchemaContext schemaContext) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("/");
        QName _nodeType = argument.getNodeType();
        CharSequence _restconfIdentifier = toRestconfIdentifier(_nodeType,schemaContext);
        _builder.append(_restconfIdentifier, "");
        return _builder;
    }
    private static CharSequence convertListToRestconfIdentifier(final InstanceIdentifier.NodeIdentifierWithPredicates argument, final ListSchemaNode node,SchemaContext schemaContext) {
        QName _nodeType = argument.getNodeType();
        final CharSequence nodeIdentifier = toRestconfIdentifier(_nodeType,schemaContext);
        final Map<QName,Object> keyValues = argument.getKeyValues();
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("/");
        _builder.append(nodeIdentifier, "");
        _builder.append("/");
        {
            List<QName> _keyDefinition = node.getKeyDefinition();
            boolean _hasElements = false;
            for(final QName key : _keyDefinition) {
                if (!_hasElements) {
                    _hasElements = true;
                } else {
                    _builder.appendImmediate("/", "");
                }
                Object _get = keyValues.get(key);
                String _uriString = toUriString(_get);
                _builder.append(_uriString, "");
            }
        }
        return _builder;
    }
    private static String toUriString(final Object object) {
        boolean _tripleEquals = (object == null);
        if (_tripleEquals) {
            return "";
        }
        String _string = object.toString();
        return URLEncoder.encode(_string);
    }

    public static CharSequence toRestconfIdentifier(final QName qname,SchemaContext schemaContext) {
        URI _namespace = qname.getNamespace();
        String module = uriToModuleName.get(_namespace);
        boolean _tripleEquals = (module == null);
        if (_tripleEquals) {
            URI _namespace_1 = qname.getNamespace();
            Date _revision = qname.getRevision();
            final Module moduleSchema = schemaContext.findModuleByNamespaceAndRevision(_namespace_1, _revision);
            boolean _tripleEquals_1 = (moduleSchema == null);
            if (_tripleEquals_1) {
                return null;
            }
            URI _namespace_2 = qname.getNamespace();
            String _name = moduleSchema.getName();
            uriToModuleName.put(_namespace_2, _name);
            String _name_1 = moduleSchema.getName();
            module = _name_1;
        }
        StringConcatenation _builder = new StringConcatenation();
        _builder.append(module, "");
        _builder.append(":");
        String _localName = qname.getLocalName();
        _builder.append(_localName, "");
        return _builder;
    }
    private static CharSequence convertToRestconfIdentifier(final InstanceIdentifier.PathArgument argument, final DataNodeContainer node, SchemaContext schemaContext) {
        if (argument instanceof InstanceIdentifier.NodeIdentifier
                && node instanceof ContainerSchemaNode) {
            return convertContainerToRestconfIdentifier((NodeIdentifier)argument, (ContainerSchemaNode) node,schemaContext);
        } else if (argument instanceof InstanceIdentifier.NodeIdentifierWithPredicates
                && node instanceof ListSchemaNode) {
            return convertListToRestconfIdentifier((NodeIdentifierWithPredicates) argument,(ListSchemaNode) node,schemaContext);
        }  else {
            throw new IllegalArgumentException("Unhandled parameter types: " +
                    Arrays.<Object>asList(argument, node).toString());
        }
    }
    private static boolean isListOrContainer(final DataSchemaNode node) {
        boolean _or = false;
        if ((node instanceof ListSchemaNode)) {
            _or = true;
        } else {
            _or = ((node instanceof ListSchemaNode) || (node instanceof ContainerSchemaNode));
        }
        return _or;
    }

    public static Module findModuleByNamespace(final URI namespace,SchemaContext schemaContext) {
        boolean _tripleNotEquals = (namespace != null);
        Preconditions.checkArgument(_tripleNotEquals);
        final Set<Module> moduleSchemas = schemaContext.findModuleByNamespace(namespace);
        Module _filterLatestModule = null;
        if (moduleSchemas!=null) {
            _filterLatestModule=filterLatestModule(moduleSchemas);
        }
        return _filterLatestModule;
    }

    private static Module filterLatestModule(final Iterable<Module> modules) {
        Module latestModule = IterableExtensions.<Module>head(modules);
        for (final Module module : modules) {
            Date _revision = module.getRevision();
            Date _revision_1 = latestModule.getRevision();
            boolean _after = _revision.after(_revision_1);
            if (_after) {
                latestModule = module;
            }
        }
        return latestModule;
    }

    public String findModuleNameByNamespace(final URI namespace,SchemaContext schemaContext) {
        String moduleName = this.uriToModuleName.get(namespace);
        boolean _tripleEquals = (moduleName == null);
        if (_tripleEquals) {
            final Module module = findModuleByNamespace(namespace, schemaContext);
            boolean _tripleEquals_1 = (module == null);
            if (_tripleEquals_1) {
                return null;
            }
            String _name = module.getName();
            moduleName = _name;
            this.uriToModuleName.put(namespace, moduleName);
        }
        return moduleName;
    }

    public static Set<Class<? extends RpcService>> rpcServicesFromInputStream(InputStream inputStream, BindingIndependentMappingService mappingService){
        try {
            DocumentBuilderFactory documentBuilder = DocumentBuilderFactory.newInstance();
            documentBuilder.setNamespaceAware(true);
            DocumentBuilder builder = documentBuilder.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            Element rootElement = doc.getDocumentElement();

            List<Node<?>> domNodes = XmlDocumentUtils.toDomNodes(rootElement, Optional.<Set<DataSchemaNode>>absent());

            Set<Class<? extends RpcService>> rpcServices = new HashSet<Class<? extends RpcService>>();
            for (Node<?> node:domNodes){
                rpcServices.add(mappingService.getRpcServiceClassFor(node.getNodeType().getNamespace().toString(),node.getNodeType().getRevision().toString()).get());
            }

            return rpcServices;
        } catch (ParserConfigurationException e) {
            logger.trace("Parse configuration exception {}",e);
        } catch (SAXException e) {
            logger.trace("SAX exception {}",e);
        } catch (IOException e) {
            logger.trace("IOException {}",e);
        }
        return null;
    }
    public static DataObject dataObjectFromInputStream(org.opendaylight.yangtools.yang.binding.InstanceIdentifier<?> path, InputStream inputStream, SchemaContext schemaContext, BindingIndependentMappingService mappingService, DataSchemaNode dataSchema) {
        // Parse stream into w3c Document
        try {
            DocumentBuilderFactory documentBuilder = DocumentBuilderFactory.newInstance();
            documentBuilder.setNamespaceAware(true);
            DocumentBuilder builder = documentBuilder.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            Element rootElement = doc.getDocumentElement();
            Node<?> domNode =  XmlDocumentUtils.toDomNode(rootElement, Optional.of(dataSchema), Optional.<XmlCodecProvider>absent());
            DataObject  dataObject = mappingService.dataObjectFromDataDom(path, (CompositeNode) domNode); //getDataFromResponse
            return dataObject;
        } catch (DeserializationException e) {


        } catch (ParserConfigurationException e) {
            logger.trace("Parse configuration exception {}",e);
        } catch (SAXException e) {
            logger.trace("SAX exception {}", e);
        } catch (IOException e) {
            logger.trace("IOException {}", e);
        }
        return null;
    }

}
