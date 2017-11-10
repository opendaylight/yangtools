/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import java.io.Closeable;
import java.io.EOFException;
import java.io.Flushable;
import java.io.IOException;
import java.net.URI;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.util.AbstractNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.AnyXmlNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafListEntryNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafListNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ListEntryNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ListNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ParserStreamUtils;
import org.opendaylight.yangtools.yang.data.util.RpcAsContainer;
import org.opendaylight.yangtools.yang.data.util.SimpleNodeDataWithSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangModeledAnyXmlSchemaNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * This class parses JSON elements from a GSON JsonReader. It disallows multiple elements of the same name unlike the
 * default GSON JsonParser.
 */
@Beta
public final class JsonParserStream implements Closeable, Flushable {
    static final String ANYXML_ARRAY_ELEMENT_ID = "array-element";

    private final Deque<URI> namespaces = new ArrayDeque<>();
    private final NormalizedNodeStreamWriter writer;
    private final JSONCodecFactory codecs;
    private final SchemaContext schema;
    private final DataSchemaNode parentNode;

    private JsonParserStream(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext,
            final JSONCodecFactory codecs, final DataSchemaNode parentNode) {
        this.schema = requireNonNull(schemaContext);
        this.writer = requireNonNull(writer);
        this.codecs = requireNonNull(codecs);
        this.parentNode = parentNode;
    }

    private JsonParserStream(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext,
            final DataSchemaNode parentNode) {
        this(writer, schemaContext, JSONCodecFactory.getShared(schemaContext), parentNode);
    }

    public static JsonParserStream create(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext,
            final SchemaNode parentNode) {
        if (parentNode instanceof RpcDefinition) {
            return new JsonParserStream(writer, schemaContext, new RpcAsContainer((RpcDefinition) parentNode));
        }
        checkArgument(parentNode instanceof DataSchemaNode, "Instance of DataSchemaNode class awaited.");
        return new JsonParserStream(writer, schemaContext, (DataSchemaNode) parentNode);
    }

    public static JsonParserStream create(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext) {
        return new JsonParserStream(writer, schemaContext, schemaContext);
    }

    public JsonParserStream parse(final JsonReader reader) {
        // code copied from gson's JsonParser and Stream classes

        final boolean lenient = reader.isLenient();
        reader.setLenient(true);
        boolean isEmpty = true;
        try {
            reader.peek();
            isEmpty = false;
            final CompositeNodeDataWithSchema compositeNodeDataWithSchema = new CompositeNodeDataWithSchema(parentNode);
            read(reader, compositeNodeDataWithSchema);
            compositeNodeDataWithSchema.write(writer);

            return this;
        } catch (final EOFException e) {
            if (isEmpty) {
                return this;
            }
            // The stream ended prematurely so it is likely a syntax error.
            throw new JsonSyntaxException(e);
        } catch (final MalformedJsonException | NumberFormatException e) {
            throw new JsonSyntaxException(e);
        } catch (final IOException e) {
            throw new JsonIOException(e);
        } catch (StackOverflowError | OutOfMemoryError e) {
            throw new JsonParseException("Failed parsing JSON source: " + reader + " to Json", e);
        } finally {
            reader.setLenient(lenient);
        }
    }

    private void traverseAnyXmlValue(final JsonReader in, final Document doc, final Element parentElement)
            throws IOException {
        switch (in.peek()) {
            case STRING:
            case NUMBER:
                Text textNode = doc.createTextNode(in.nextString());
                parentElement.appendChild(textNode);
                break;
            case BOOLEAN:
                textNode = doc.createTextNode(Boolean.toString(in.nextBoolean()));
                parentElement.appendChild(textNode);
                break;
            case NULL:
                in.nextNull();
                textNode = doc.createTextNode("null");
                parentElement.appendChild(textNode);
                break;
            case BEGIN_ARRAY:
                in.beginArray();
                while (in.hasNext()) {
                    final Element childElement = doc.createElement(ANYXML_ARRAY_ELEMENT_ID);
                    parentElement.appendChild(childElement);
                    traverseAnyXmlValue(in, doc, childElement);
                }
                in.endArray();
                break;
            case BEGIN_OBJECT:
                in.beginObject();
                while (in.hasNext()) {
                    final Element childElement = doc.createElement(in.nextName());
                    parentElement.appendChild(childElement);
                    traverseAnyXmlValue(in, doc, childElement);
                }
                in.endObject();
                break;
            default:
                break;
        }
    }

    private void readAnyXmlValue(final JsonReader in, final AnyXmlNodeDataWithSchema parent,
            final String anyXmlObjectName) throws IOException {
        final String anyXmlObjectNS = getCurrentNamespace().toString();
        final Document doc = UntrustedXML.newDocumentBuilder().newDocument();
        final Element rootElement = doc.createElementNS(anyXmlObjectNS, anyXmlObjectName);
        doc.appendChild(rootElement);
        traverseAnyXmlValue(in, doc, rootElement);

        final DOMSource domSource = new DOMSource(doc.getDocumentElement());
        parent.setValue(domSource);
    }

    public void read(final JsonReader in, AbstractNodeDataWithSchema parent) throws IOException {
        switch (in.peek()) {
            case STRING:
            case NUMBER:
                setValue(parent, in.nextString());
                break;
            case BOOLEAN:
                setValue(parent, Boolean.toString(in.nextBoolean()));
                break;
            case NULL:
                in.nextNull();
                setValue(parent, null);
                break;
            case BEGIN_ARRAY:
                in.beginArray();
                while (in.hasNext()) {
                    if (parent instanceof LeafNodeDataWithSchema) {
                        read(in, parent);
                    } else {
                        final AbstractNodeDataWithSchema newChild = newArrayEntry(parent);
                        read(in, newChild);
                    }
                }
                in.endArray();
                return;
            case BEGIN_OBJECT:
                final Set<String> namesakes = new HashSet<>();
                in.beginObject();
                /*
                 * This allows parsing of incorrectly /as showcased/
                 * in testconf nesting of list items - eg.
                 * lists with one value are sometimes serialized
                 * without wrapping array.
                 *
                 */
                if (isArray(parent)) {
                    parent = newArrayEntry(parent);
                }
                while (in.hasNext()) {
                    final String jsonElementName = in.nextName();
                    DataSchemaNode parentSchema = parent.getSchema();
                    if (parentSchema instanceof YangModeledAnyXmlSchemaNode) {
                        parentSchema = ((YangModeledAnyXmlSchemaNode) parentSchema).getSchemaOfAnyXmlData();
                    }
                    final Entry<String, URI> namespaceAndName = resolveNamespace(jsonElementName, parentSchema);
                    final String localName = namespaceAndName.getKey();
                    addNamespace(namespaceAndName.getValue());
                    if (!namesakes.add(jsonElementName)) {
                        throw new JsonSyntaxException("Duplicate name " + jsonElementName + " in JSON input.");
                    }

                    final Deque<DataSchemaNode> childDataSchemaNodes =
                            ParserStreamUtils.findSchemaNodeByNameAndNamespace(parentSchema, localName,
                                getCurrentNamespace());
                    checkState(!childDataSchemaNodes.isEmpty(),
                        "Schema for node with name %s and namespace %s does not exist.", localName,
                        getCurrentNamespace());

                    final AbstractNodeDataWithSchema newChild = ((CompositeNodeDataWithSchema) parent)
                            .addChild(childDataSchemaNodes);
                    if (newChild instanceof AnyXmlNodeDataWithSchema) {
                        readAnyXmlValue(in, (AnyXmlNodeDataWithSchema) newChild, jsonElementName);
                    } else {
                        read(in, newChild);
                    }
                    removeNamespace();
                }
                in.endObject();
                return;
            default:
                break;
        }
    }

    private static boolean isArray(final AbstractNodeDataWithSchema parent) {
        return parent instanceof ListNodeDataWithSchema || parent instanceof LeafListNodeDataWithSchema;
    }

    private static AbstractNodeDataWithSchema newArrayEntry(final AbstractNodeDataWithSchema parent) {
        AbstractNodeDataWithSchema newChild;
        if (parent instanceof ListNodeDataWithSchema) {
            newChild = new ListEntryNodeDataWithSchema(parent.getSchema());
        } else if (parent instanceof LeafListNodeDataWithSchema) {
            newChild = new LeafListEntryNodeDataWithSchema(parent.getSchema());
        } else {
            throw new IllegalStateException("Found an unexpected array nested under " + parent.getSchema().getQName());
        }
        ((CompositeNodeDataWithSchema) parent).addChild(newChild);
        return newChild;
    }

    private void setValue(final AbstractNodeDataWithSchema parent, final String value) {
        checkArgument(parent instanceof SimpleNodeDataWithSchema, "Node %s is not a simple type",
                parent.getSchema().getQName());
        final SimpleNodeDataWithSchema parentSimpleNode = (SimpleNodeDataWithSchema) parent;
        checkArgument(parentSimpleNode.getValue() == null, "Node '%s' has already set its value to '%s'",
                parentSimpleNode.getSchema().getQName(), parentSimpleNode.getValue());

        final Object translatedValue = translateValueByType(value, parentSimpleNode.getSchema());
        parentSimpleNode.setValue(translatedValue);
    }

    private Object translateValueByType(final String value, final DataSchemaNode node) {
        checkArgument(node instanceof TypedDataSchemaNode);
        return codecs.codecFor((TypedDataSchemaNode) node).parseValue(null, value);
    }

    private void removeNamespace() {
        namespaces.pop();
    }

    private void addNamespace(final URI namespace) {
        namespaces.push(namespace);
    }

    private Entry<String, URI> resolveNamespace(final String childName, final DataSchemaNode dataSchemaNode) {
        final int lastIndexOfColon = childName.lastIndexOf(':');
        String moduleNamePart = null;
        String nodeNamePart = null;
        URI namespace = null;
        if (lastIndexOfColon != -1) {
            moduleNamePart = childName.substring(0, lastIndexOfColon);
            nodeNamePart = childName.substring(lastIndexOfColon + 1);

            final Iterator<Module> m = schema.findModules(moduleNamePart).iterator();
            namespace = m.hasNext() ? m.next().getNamespace() : null;
        } else {
            nodeNamePart = childName;
        }

        if (namespace == null) {
            Set<URI> potentialUris = Collections.emptySet();
            potentialUris = resolveAllPotentialNamespaces(nodeNamePart, dataSchemaNode);
            if (potentialUris.contains(getCurrentNamespace())) {
                namespace = getCurrentNamespace();
            } else if (potentialUris.size() == 1) {
                namespace = potentialUris.iterator().next();
            } else if (potentialUris.size() > 1) {
                throw new IllegalStateException("Choose suitable module name for element " + nodeNamePart + ":"
                        + toModuleNames(potentialUris));
            } else if (potentialUris.isEmpty()) {
                throw new IllegalStateException("Schema node with name " + nodeNamePart + " was not found under "
                        + dataSchemaNode.getQName() + ".");
            }
        }

        return new SimpleImmutableEntry<>(nodeNamePart, namespace);
    }

    private String toModuleNames(final Set<URI> potentialUris) {
        final StringBuilder builder = new StringBuilder();
        for (final URI potentialUri : potentialUris) {
            builder.append('\n');
            //FIXME how to get information about revision from JSON input? currently first available is used.
            builder.append(schema.findModules(potentialUri).iterator().next().getName());
        }
        return builder.toString();
    }

    private Set<URI> resolveAllPotentialNamespaces(final String elementName, final DataSchemaNode dataSchemaNode) {
        final Set<URI> potentialUris = new HashSet<>();
        final Set<ChoiceSchemaNode> choices = new HashSet<>();
        if (dataSchemaNode instanceof DataNodeContainer) {
            for (final DataSchemaNode childSchemaNode : ((DataNodeContainer) dataSchemaNode).getChildNodes()) {
                if (childSchemaNode instanceof ChoiceSchemaNode) {
                    choices.add((ChoiceSchemaNode)childSchemaNode);
                } else if (childSchemaNode.getQName().getLocalName().equals(elementName)) {
                    potentialUris.add(childSchemaNode.getQName().getNamespace());
                }
            }

            for (final ChoiceSchemaNode choiceNode : choices) {
                for (final ChoiceCaseNode concreteCase : choiceNode.getCases().values()) {
                    potentialUris.addAll(resolveAllPotentialNamespaces(elementName, concreteCase));
                }
            }
        }
        return potentialUris;
    }

    private URI getCurrentNamespace() {
        return namespaces.peek();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }
}
