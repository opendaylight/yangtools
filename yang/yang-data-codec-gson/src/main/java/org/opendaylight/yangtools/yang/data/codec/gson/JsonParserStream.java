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
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.model.api.YangModeledAnyxmlSchemaNode;
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
import org.opendaylight.yangtools.yang.data.util.MultipleEntryDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.OperationAsContainer;
import org.opendaylight.yangtools.yang.data.util.ParserStreamUtils;
import org.opendaylight.yangtools.yang.data.util.SimpleNodeDataWithSchema;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(JsonParserStream.class);
    private final Deque<URI> namespaces = new ArrayDeque<>();
    private final NormalizedNodeStreamWriter writer;
    private final JSONCodecFactory codecs;
    private final DataSchemaNode parentNode;

    // TODO: consider class specialization to remove this field
    private final boolean lenient;

    private JsonParserStream(final NormalizedNodeStreamWriter writer, final JSONCodecFactory codecs,
            final DataSchemaNode parentNode, final boolean lenient) {
        this.writer = requireNonNull(writer);
        this.codecs = requireNonNull(codecs);
        this.parentNode = parentNode;
        this.lenient = lenient;
    }

    /**
     * Create a new {@link JsonParserStream} backed by specified {@link NormalizedNodeStreamWriter}
     * and {@link JSONCodecFactory}. The stream will be logically rooted at the top of the SchemaContext associated
     * with the specified codec factory.
     *
     * @param writer NormalizedNodeStreamWriter to use for instantiation of normalized nodes
     * @param codecFactory {@link JSONCodecFactory} to use for parsing leaves
     * @return A new {@link JsonParserStream}
     * @throws NullPointerException if any of the arguments are null
     */
    public static @NonNull JsonParserStream create(final @NonNull NormalizedNodeStreamWriter writer,
            final @NonNull JSONCodecFactory codecFactory) {
        return new JsonParserStream(writer, codecFactory, codecFactory.getEffectiveModelContext(), false);
    }

    /**
     * Create a new {@link JsonParserStream} backed by specified {@link NormalizedNodeStreamWriter}
     * and {@link JSONCodecFactory}. The stream will be logically rooted at the specified parent node.
     *
     * @param writer NormalizedNodeStreamWriter to use for instantiation of normalized nodes
     * @param codecFactory {@link JSONCodecFactory} to use for parsing leaves
     * @param parentNode Logical root node
     * @return A new {@link JsonParserStream}
     * @throws NullPointerException if any of the arguments are null
     */
    public static @NonNull JsonParserStream create(final @NonNull NormalizedNodeStreamWriter writer,
            final @NonNull JSONCodecFactory codecFactory, final @NonNull SchemaNode parentNode) {
        return new JsonParserStream(writer, codecFactory, validateParent(parentNode), false);
    }

    /**
     * Create a new {@link JsonParserStream} backed by specified {@link NormalizedNodeStreamWriter}
     * and {@link JSONCodecFactory}. The stream will be logically rooted at the top of the SchemaContext associated
     * with the specified codec factory.
     *
     * <p>
     * Returned parser will treat incoming JSON data leniently:
     * <ul>
     *   <li>JSON elements referring to unknown constructs will be silently ignored</li>
     * </ul>
     *
     * @param writer NormalizedNodeStreamWriter to use for instantiation of normalized nodes
     * @param codecFactory {@link JSONCodecFactory} to use for parsing leaves
     * @return A new {@link JsonParserStream}
     * @throws NullPointerException if any of the arguments are null
     */
    public static @NonNull JsonParserStream createLenient(final @NonNull NormalizedNodeStreamWriter writer,
            final @NonNull JSONCodecFactory codecFactory) {
        return new JsonParserStream(writer, codecFactory, codecFactory.getEffectiveModelContext(), true);
    }

    /**
     * Create a new {@link JsonParserStream} backed by specified {@link NormalizedNodeStreamWriter}
     * and {@link JSONCodecFactory}. The stream will be logically rooted at the specified parent node.
     *
     * <p>
     * Returned parser will treat incoming JSON data leniently:
     * <ul>
     *   <li>JSON elements referring to unknown constructs will be silently ignored</li>
     * </ul>
     *
     * @param writer NormalizedNodeStreamWriter to use for instantiation of normalized nodes
     * @param codecFactory {@link JSONCodecFactory} to use for parsing leaves
     * @param parentNode Logical root node
     * @return A new {@link JsonParserStream}
     * @throws NullPointerException if any of the arguments are null
     */
    public static @NonNull JsonParserStream createLenient(final @NonNull NormalizedNodeStreamWriter writer,
            final @NonNull JSONCodecFactory codecFactory, final @NonNull SchemaNode parentNode) {
        return new JsonParserStream(writer, codecFactory, validateParent(parentNode), true);
    }

    public JsonParserStream parse(final JsonReader reader) {
        // code copied from gson's JsonParser and Stream classes
        final boolean readerLenient = reader.isLenient();
        reader.setLenient(true);
        boolean isEmpty = true;
        try {
            reader.peek();
            isEmpty = false;
            final CompositeNodeDataWithSchema<?> compositeNodeDataWithSchema =
                    new CompositeNodeDataWithSchema<>(parentNode);
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
            reader.setLenient(readerLenient);
        }
    }

    private void traverseAnyXmlValue(final JsonReader in, final Document doc, final Element parent) throws IOException {
        final Deque<Element> nodes = new ArrayDeque<>();
        nodes.push(parent);
        while (!nodes.isEmpty()) {
            switch (in.peek()) {
                case STRING:
                case NUMBER:
                    Text textNode = doc.createTextNode(in.nextString());
                    nodes.getFirst().appendChild(textNode);
                    createAnyXmlElementIfHasNext(in, doc, nodes);
                    break;
                case BOOLEAN:
                    textNode = doc.createTextNode(Boolean.toString(in.nextBoolean()));
                    nodes.getFirst().appendChild(textNode);
                    createAnyXmlElementIfHasNext(in, doc, nodes);
                    break;
                case NULL:
                    in.nextNull();
                    textNode = doc.createTextNode("null");
                    nodes.getFirst().appendChild(textNode);
                    createAnyXmlElementIfHasNext(in, doc, nodes);
                    break;
                case BEGIN_ARRAY:
                    in.beginArray();
                    if (in.hasNext()) {
                        createAnyXmlElement(doc, nodes, ANYXML_ARRAY_ELEMENT_ID);
                    }
                    break;
                case END_ARRAY:
                    in.endArray();
                    createAnyXmlElementIfHasNext(in, doc, nodes);
                    break;
                case BEGIN_OBJECT:
                    in.beginObject();
                    break;
                case NAME:
                    createAnyXmlElement(doc, nodes, in.nextName());
                    break;
                case END_OBJECT:
                    in.endObject();
                    createAnyXmlElementIfHasNext(in, doc, nodes);
                    break;
                case END_DOCUMENT:
                    return;
                default:
                    break;
            }
        }
    }

    private void createAnyXmlElementIfHasNext(JsonReader in, Document doc, Deque<Element> nodes) throws IOException {
        if (nodes.pop().getTagName().equals(ANYXML_ARRAY_ELEMENT_ID) && in.hasNext()) {
            createAnyXmlElement(doc, nodes, ANYXML_ARRAY_ELEMENT_ID);
        }
    }

    private void createAnyXmlElement(final Document doc, final Deque<Element> nodes, final String element) {
        final Element childElement = doc.createElement(element);
        nodes.getFirst().appendChild(childElement);
        nodes.push(childElement);
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

    private void setNodeValue(final JsonReader in, final Deque<AbstractNodeDataWithSchema> nodes, final String value)
        throws IOException {
        setValue(nodes.getFirst(), value);
        if (nodes.pop() instanceof LeafListEntryNodeDataWithSchema) {
            if (in.hasNext()) {
                // if last element was leaf-list and there is more values, create next element
                nodes.push(newArrayEntry(nodes.getFirst()));
            }
        } else {
            removeNamespace();
        }
    }

    public void read(final JsonReader in, AbstractNodeDataWithSchema<?> parent) throws IOException {
        final Deque<Set<String>> nodeNamesakes = new ArrayDeque<>();
        final Deque<AbstractNodeDataWithSchema> nodes = new ArrayDeque<>();
        nodes.push(parent);
        while (!nodes.isEmpty()) {
            switch (in.peek()) {
                case STRING:
                case NUMBER:
                    setNodeValue(in, nodes, in.nextString());
                    break;
                case BOOLEAN:
                    setNodeValue(in, nodes, Boolean.toString(in.nextBoolean()));
                    break;
                case NULL:
                    in.nextNull();
                    setNodeValue(in, nodes, null);
                    break;
                case BEGIN_ARRAY:
                    in.beginArray();
                    if (in.hasNext()) {
                        if (nodes.getFirst() instanceof LeafNodeDataWithSchema) {
                            nodes.push(nodes.getFirst());
                        } else {
                            nodes.push(newArrayEntry(nodes.getFirst()));
                        }
                    }
                    break;
                case END_ARRAY:
                    in.endArray();
                    if (nodes.pop() instanceof LeafListNodeDataWithSchema) {
                        removeNamespace();
                    }
                    break;
                case BEGIN_OBJECT:
                    in.beginObject();
                    /*
                     * This allows parsing of incorrectly /as showcased/
                     * in testconf nesting of list items - eg.
                     * lists with one value are sometimes serialized
                     * without wrapping array.
                     *
                     */
                    if (isArray(nodes.getFirst())) {
                        nodes.push(newArrayEntry(nodes.pop()));
                    }
                    nodeNamesakes.push(new HashSet<>());
                    break;
                case NAME:
                    final String jsonElementName = in.nextName();
                    DataSchemaNode parentSchema = nodes.getFirst().getSchema();
                    if (parentSchema instanceof YangModeledAnyxmlSchemaNode) {
                        parentSchema = ((YangModeledAnyxmlSchemaNode) parentSchema).getSchemaOfAnyXmlData();
                    }
                    final Entry<String, URI> namespaceAndName = resolveNamespace(jsonElementName, parentSchema);
                    final String localName = namespaceAndName.getKey();
                    final URI namespace = namespaceAndName.getValue();
                    if (lenient && (localName == null || namespace == null)) {
                        LOG.debug("Schema node with name {} was not found under {}", localName,
                                parentSchema.getQName());
                        in.skipValue();
                        continue;
                    }
                    addNamespace(namespace);
                    if (!nodeNamesakes.getFirst().add(jsonElementName)) {
                        throw new JsonSyntaxException("Duplicate name " + jsonElementName + " in JSON input.");
                    }

                    final Deque<DataSchemaNode> childDataSchemaNodes =
                            ParserStreamUtils.findSchemaNodeByNameAndNamespace(parentSchema, localName,
                                    getCurrentNamespace());
                    checkState(!childDataSchemaNodes.isEmpty(),
                            "Schema for node with name %s and namespace %s does not exist at %s",
                            localName, getCurrentNamespace(), parentSchema);

                    final AbstractNodeDataWithSchema<?> newChild = ((CompositeNodeDataWithSchema<?>)
                            nodes.getFirst()).addChild(childDataSchemaNodes);
                    if (newChild instanceof AnyXmlNodeDataWithSchema) {
                        readAnyXmlValue(in, (AnyXmlNodeDataWithSchema) newChild, jsonElementName);
                        removeNamespace();
                    } else {
                        nodes.push(newChild);
                    }
                    break;
                case END_OBJECT:
                    in.endObject();
                    if (nodes.pop() instanceof ListEntryNodeDataWithSchema && in.hasNext()) {
                        // if last element was list entry node and there is more values, create next element
                        nodes.push(newArrayEntry(nodes.getFirst()));
                    } else {
                        // at the end of the document there is no namespace to remove
                        if (!nodes.isEmpty()) {
                            removeNamespace();
                        }
                    }
                    nodeNamesakes.pop();
                    break;
                default:
                    break;
            }
        }
    }

    private static boolean isArray(final AbstractNodeDataWithSchema<?> parent) {
        return parent instanceof ListNodeDataWithSchema || parent instanceof LeafListNodeDataWithSchema;
    }

    private static AbstractNodeDataWithSchema<?> newArrayEntry(final AbstractNodeDataWithSchema<?> parent) {
        if (!(parent instanceof MultipleEntryDataWithSchema)) {
            throw new IllegalStateException("Found an unexpected array nested under " + parent.getSchema().getQName());
        }
        return ((MultipleEntryDataWithSchema<?>) parent).newChildEntry();
    }

    private void setValue(final AbstractNodeDataWithSchema<?> parent, final String value) {
        checkArgument(parent instanceof SimpleNodeDataWithSchema, "Node %s is not a simple type",
                parent.getSchema().getQName());
        final SimpleNodeDataWithSchema<?> parentSimpleNode = (SimpleNodeDataWithSchema<?>) parent;
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

            final Iterator<? extends Module> m = codecs.getEffectiveModelContext().findModules(moduleNamePart)
                    .iterator();
            namespace = m.hasNext() ? m.next().getNamespace() : null;
        } else {
            nodeNamePart = childName;
        }

        if (namespace == null) {
            final Set<URI> potentialUris = resolveAllPotentialNamespaces(nodeNamePart, dataSchemaNode);
            if (potentialUris.contains(getCurrentNamespace())) {
                namespace = getCurrentNamespace();
            } else if (potentialUris.size() == 1) {
                namespace = potentialUris.iterator().next();
            } else if (potentialUris.size() > 1) {
                throw new IllegalStateException("Choose suitable module name for element " + nodeNamePart + ":"
                        + toModuleNames(potentialUris));
            } else if (potentialUris.isEmpty() && !lenient) {
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
            builder.append(codecs.getEffectiveModelContext().findModules(potentialUri).iterator().next().getName());
        }
        return builder.toString();
    }

    private Set<URI> resolveAllPotentialNamespaces(final String elementName, final DataSchemaNode dataSchemaNode) {
        final Set<URI> potentialUris = new HashSet<>();
        final Set<ChoiceSchemaNode> choices = new HashSet<>();
        if (dataSchemaNode instanceof DataNodeContainer) {
            for (final DataSchemaNode childSchemaNode : ((DataNodeContainer) dataSchemaNode).getChildNodes()) {
                if (childSchemaNode instanceof ChoiceSchemaNode) {
                    choices.add((ChoiceSchemaNode) childSchemaNode);
                } else if (childSchemaNode.getQName().getLocalName().equals(elementName)) {
                    potentialUris.add(childSchemaNode.getQName().getNamespace());
                }
            }

            for (final ChoiceSchemaNode choiceNode : choices) {
                for (final CaseSchemaNode concreteCase : choiceNode.getCases()) {
                    potentialUris.addAll(resolveAllPotentialNamespaces(elementName, concreteCase));
                }
            }
        }
        return potentialUris;
    }

    private URI getCurrentNamespace() {
        return namespaces.peek();
    }

    private static DataSchemaNode validateParent(final SchemaNode parent) {
        if (parent instanceof DataSchemaNode) {
            return (DataSchemaNode) parent;
        } else if (parent instanceof OperationDefinition) {
            return OperationAsContainer.of((OperationDefinition) parent);
        } else {
            throw new IllegalArgumentException("Illegal parent node " + requireNonNull(parent));
        }
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
