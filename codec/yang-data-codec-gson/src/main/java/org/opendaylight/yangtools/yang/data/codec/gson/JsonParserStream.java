/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static java.util.Objects.requireNonNull;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import java.io.Closeable;
import java.io.EOFException;
import java.io.Flushable;
import java.io.IOException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataSchemaNode;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.util.AbstractNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.AnyXmlNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema.ChildReusePolicy;
import org.opendaylight.yangtools.yang.data.util.LeafListNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ListNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.MultipleEntryDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ParserStreamUtils;
import org.opendaylight.yangtools.yang.data.util.SimpleNodeDataWithSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * This class parses JSON elements from a GSON JsonReader. It disallows multiple elements of the same name unlike the
 * default GSON JsonParser.
 */
public final class JsonParserStream implements Closeable, Flushable {
    static final String ANYXML_ARRAY_ELEMENT_ID = "array-element";

    private static final Logger LOG = LoggerFactory.getLogger(JsonParserStream.class);
    private final Deque<XMLNamespace> namespaces = new ArrayDeque<>();
    private final NormalizedNodeStreamWriter writer;
    private final JSONCodecFactory codecs;
    private final DataSchemaNode parentNode;

    private final SchemaInferenceStack stack;

    // TODO: consider class specialization to remove this field
    private final boolean lenient;

    private JsonParserStream(final NormalizedNodeStreamWriter writer, final JSONCodecFactory codecs,
            final SchemaInferenceStack stack, final boolean lenient) {
        this.writer = requireNonNull(writer);
        this.codecs = requireNonNull(codecs);
        this.stack = requireNonNull(stack);
        this.lenient = lenient;

        if (!stack.isEmpty()) {
            final EffectiveStatement<?, ?> parent = stack.currentStatement();
            if (parent instanceof DataSchemaNode data) {
                parentNode = data;
            } else if (parent instanceof OperationDefinition oper) {
                parentNode = oper.toContainerLike();
            } else if (parent instanceof NotificationDefinition notif) {
                parentNode = notif.toContainerLike();
            } else if (parent instanceof YangDataSchemaNode yangData) {
                parentNode = yangData.toContainerLike();
            } else {
                throw new IllegalArgumentException("Illegal parent node " + parent);
            }
        } else {
            parentNode = stack.getEffectiveModelContext();
        }
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
        return new JsonParserStream(writer, codecFactory,
            SchemaInferenceStack.of(codecFactory.getEffectiveModelContext()), false);
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
            final @NonNull JSONCodecFactory codecFactory, final @NonNull EffectiveStatementInference parentNode) {
        return new JsonParserStream(writer, codecFactory, SchemaInferenceStack.ofInference(parentNode), false);
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
        return new JsonParserStream(writer, codecFactory,
            SchemaInferenceStack.of(codecFactory.getEffectiveModelContext()), true);
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
            final @NonNull JSONCodecFactory codecFactory, final @NonNull EffectiveStatementInference parentNode) {
        return new JsonParserStream(writer, codecFactory, SchemaInferenceStack.ofInference(parentNode), true);
    }

    public JsonParserStream parse(final JsonReader reader) {
        // code copied from gson's JsonParser and Stream classes

        final boolean readerLenient = reader.isLenient();
        reader.setLenient(true);
        boolean isEmpty = true;
        try {
            reader.peek();
            isEmpty = false;
            // FIXME: this has a special-case bypass for SchemaContext, where we end up emitting just the child while
            //        the usual of() would result in SchemaContext.NAME being the root
            final var compositeNodeDataWithSchema = new CompositeNodeDataWithSchema<>(parentNode);
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
                    final var childElement = doc.createElement(ANYXML_ARRAY_ELEMENT_ID);
                    parentElement.appendChild(childElement);
                    traverseAnyXmlValue(in, doc, childElement);
                }
                in.endArray();
                break;
            case BEGIN_OBJECT:
                in.beginObject();
                while (in.hasNext()) {
                    final var childElement = doc.createElement(in.nextName());
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
        final var doc = UntrustedXML.newDocumentBuilder().newDocument();
        final var rootElement = doc.createElementNS(getCurrentNamespace().toString(), anyXmlObjectName);
        doc.appendChild(rootElement);
        traverseAnyXmlValue(in, doc, rootElement);

        parent.setValue(new DOMSource(doc.getDocumentElement()));
    }

    private void read(final JsonReader in, AbstractNodeDataWithSchema<?> parent) throws IOException {
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
                        read(in, newArrayEntry(parent));
                    }
                }
                in.endArray();
                return;
            case BEGIN_OBJECT:
                final var namesakes = new HashSet<String>();
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
                    final var jsonElementName = in.nextName();
                    final var parentSchema = parent.getSchema();
                    final var namespaceAndName = resolveNamespace(jsonElementName, parentSchema);
                    final var localName = namespaceAndName.getKey();
                    final var namespace = namespaceAndName.getValue();
                    if (lenient && (localName == null || namespace == null)) {
                        LOG.debug("Schema node with name {} was not found under {}", localName,
                            parentSchema.getQName());
                        in.skipValue();
                        continue;
                    }
                    addNamespace(namespace);
                    if (!namesakes.add(jsonElementName)) {
                        throw new JsonSyntaxException("Duplicate name " + jsonElementName + " in JSON input.");
                    }

                    final var childDataSchemaNodes = ParserStreamUtils.findSchemaNodeByNameAndNamespace(parentSchema,
                        localName, getCurrentNamespace());
                    if (childDataSchemaNodes.isEmpty()) {
                        throw new IllegalStateException(
                            "Schema for node with name %s and namespace %s does not exist at %s".formatted(
                                localName, getCurrentNamespace(), parentSchema));
                    }

                    final var qname = childDataSchemaNodes.peekLast().getQName();
                    final var newChild = ((CompositeNodeDataWithSchema<?>) parent)
                            .addChild(childDataSchemaNodes, ChildReusePolicy.NOOP);
                    if (newChild instanceof AnyXmlNodeDataWithSchema anyxml) {
                        readAnyXmlValue(in, anyxml, jsonElementName);
                    } else {
                        stack.enterDataTree(qname);
                        read(in, newChild);
                        stack.exit();
                    }
                    removeNamespace();
                }
                in.endObject();
                return;
            default:
                break;
        }
    }

    private static boolean isArray(final AbstractNodeDataWithSchema<?> parent) {
        return parent instanceof ListNodeDataWithSchema || parent instanceof LeafListNodeDataWithSchema;
    }

    private static AbstractNodeDataWithSchema<?> newArrayEntry(final AbstractNodeDataWithSchema<?> parent) {
        if (parent instanceof MultipleEntryDataWithSchema<?> multiple) {
            return multiple.newChildEntry();
        }
        throw new IllegalStateException("Found an unexpected array nested under " + parent.getSchema().getQName());
    }

    private void setValue(final AbstractNodeDataWithSchema<?> parent, final String value) {
        if (!(parent instanceof SimpleNodeDataWithSchema<?> parentSimpleNode)) {
            throw new IllegalArgumentException("Node " + parent.getSchema().getQName() + " is not a simple type");
        }
        final var prevValue = parentSimpleNode.getValue();
        if (prevValue != null) {
            throw new IllegalArgumentException("Node '%s' has already set its value to '%s'".formatted(
                parentSimpleNode.getSchema().getQName(), prevValue));
        }

        final var translatedValue = translateValueByType(value, parentSimpleNode.getSchema());
        parentSimpleNode.setValue(translatedValue);
    }

    private Object translateValueByType(final String value, final DataSchemaNode node) {
        if (node instanceof TypedDataSchemaNode typedNode) {
            return codecs.codecFor(typedNode, stack).parseValue(value);
        }
        throw new IllegalArgumentException("Unexpected node " + node);
    }

    private void removeNamespace() {
        namespaces.pop();
    }

    private void addNamespace(final XMLNamespace namespace) {
        namespaces.push(namespace);
    }

    private Entry<String, XMLNamespace> resolveNamespace(final String childName, final DataSchemaNode dataSchemaNode) {
        final int lastIndexOfColon = childName.lastIndexOf(':');
        final String nodeNamePart;
        XMLNamespace namespace;
        if (lastIndexOfColon != -1) {
            final var moduleNamePart = childName.substring(0, lastIndexOfColon);
            nodeNamePart = childName.substring(lastIndexOfColon + 1);

            final var m = codecs.getEffectiveModelContext().findModuleStatements(moduleNamePart).iterator();
            namespace = m.hasNext() ? m.next().localQNameModule().getNamespace() : null;
        } else {
            nodeNamePart = childName;
            namespace = null;
        }

        if (namespace == null) {
            final var potentialUris = resolveAllPotentialNamespaces(nodeNamePart, dataSchemaNode);
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

    private String toModuleNames(final Set<XMLNamespace> potentialUris) {
        final var sb = new StringBuilder();
        for (var potentialUri : potentialUris) {
            sb.append('\n');
            // FIXME how to get information about revision from JSON input? currently first available is used.
            sb.append(codecs.getEffectiveModelContext().findModuleStatements(potentialUri).iterator().next()
                .argument().getLocalName());
        }
        return sb.toString();
    }

    private Set<XMLNamespace> resolveAllPotentialNamespaces(final String elementName,
            final DataSchemaNode dataSchemaNode) {
        final var potentialUris = new HashSet<XMLNamespace>();
        final var choices = new HashSet<ChoiceSchemaNode>();
        if (dataSchemaNode instanceof DataNodeContainer container) {
            for (var childSchemaNode : container.getChildNodes()) {
                if (childSchemaNode instanceof ChoiceSchemaNode choice) {
                    choices.add(choice);
                } else if (childSchemaNode.getQName().getLocalName().equals(elementName)) {
                    potentialUris.add(childSchemaNode.getQName().getNamespace());
                }
            }

            for (var choiceNode : choices) {
                for (var concreteCase : choiceNode.getCases()) {
                    potentialUris.addAll(resolveAllPotentialNamespaces(elementName, concreteCase));
                }
            }
        }
        return potentialUris;
    }

    private XMLNamespace getCurrentNamespace() {
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
