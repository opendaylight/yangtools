/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static java.util.Objects.requireNonNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
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
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DataSchemaCompat;
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

    /**
     * Global cache of schema-lookup results, keyed by parent {@link DataSchemaNode} identity. Lookups are pure
     * functions of the parent node's immutable child set, hence results are shared by all parser instances.
     * {@link CacheBuilder#weakKeys()} provides the identity-based lookup and reclaims an entry once its parent node
     * becomes unreachable.
     */
    private static final LoadingCache<DataSchemaNode, SchemaNodeCache> CACHE = CacheBuilder.newBuilder()
        .weakKeys().build(CacheLoader.from(SchemaNodeCache::new));

    /**
     * Memoized schema-lookup results for a single parent {@link DataSchemaNode}, turning the linear scans otherwise
     * performed for every JSON element at the same schema level into amortized O(1) lookups. Each method takes that
     * parent node as its first argument, keeping {@link #CACHE} entries reclaimable. Only resolved lookups are
     * memoized, as both maps are keyed by names taken directly from the JSON input.
     */
    private static final class SchemaNodeCache {
        private final ConcurrentHashMap<ChildLookupKey, ImmutableList<DataSchemaNode>> resolvedPaths =
            new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, ImmutableSet<XMLNamespace>> namespacesByName =
            new ConcurrentHashMap<>();

        /**
         * Equivalent of {@link ParserStreamUtils#findSchemaNodeByNameAndNamespace(DataSchemaNode, String,
         * XMLNamespace)}, but memoized. The returned {@link Deque} is always safe to modify, as
         * {@link CompositeNodeDataWithSchema#addChild(Deque, ChildReusePolicy)} consumes it via {@code pop()}.
         *
         * @param node parent node this instance was looked up with
         */
        Deque<DataSchemaNode> findSchemaNode(final DataSchemaNode node, final String localName,
                final XMLNamespace namespace) {
            final var key = new ChildLookupKey(localName, namespace);
            final var cached = resolvedPaths.get(key);
            if (cached != null) {
                return new ArrayDeque<>(cached);
            }

            // Either [child] for a direct child, or [choice, case, ..., child] when the match is nested inside a
            // choice/case - which is why this is a path and not a single node.
            final var found = ParserStreamUtils.findSchemaNodeByNameAndNamespace(node, localName, namespace);
            if (!found.isEmpty()) {
                resolvedPaths.putIfAbsent(key, ImmutableList.copyOf(found));
            }
            // 'found' is freshly allocated and shared with nobody, hand it over as-is
            return found;
        }

        /**
         * Equivalent of {@link JsonParserStream#computePotentialNamespaces(DataSchemaNode, String)}, but memoized.
         *
         * @param node parent node this instance was looked up with
         */
        Set<XMLNamespace> potentialNamespaces(final DataSchemaNode node, final String elementName) {
            final var cached = namespacesByName.get(elementName);
            if (cached != null) {
                return cached;
            }

            final var computed = computePotentialNamespaces(node, elementName);
            if (computed.isEmpty()) {
                return computed;
            }

            // Prefer whoever won the race, so that all callers observe the same instance
            final var raced = namespacesByName.putIfAbsent(elementName, computed);
            return raced != null ? raced : computed;
        }
    }

    /**
     * Lookup key for {@link SchemaNodeCache#findSchemaNode(DataSchemaNode, String, XMLNamespace)}. Both components
     * compare by value, which is what makes this safe to use as a map key.
     */
    private record ChildLookupKey(String localName, XMLNamespace namespace) {
        ChildLookupKey {
            requireNonNull(localName);
            requireNonNull(namespace);
        }
    }

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
            final var parent = stack.currentStatement();
            parentNode = switch (parent) {
                case DataSchemaNode data -> data;
                case DataSchemaCompat<?, ?> compat -> compat.toDataSchemaNode();
                default -> throw new IllegalArgumentException("Illegal parent node " + parent);
            };
        } else {
            parentNode = stack.modelContext();
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
            SchemaInferenceStack.of(codecFactory.modelContext()), false);
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
     * <p>Returned parser will treat incoming JSON data leniently:
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
            SchemaInferenceStack.of(codecFactory.modelContext()), true);
    }

    /**
     * Create a new {@link JsonParserStream} backed by specified {@link NormalizedNodeStreamWriter}
     * and {@link JSONCodecFactory}. The stream will be logically rooted at the specified parent node.
     *
     * <p>Returned parser will treat incoming JSON data leniently:
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
                // Both are loop invariants: 'parent' is not reassigned below
                final var parentSchema = parent.getSchema();
                final var schemaCache = CACHE.getUnchecked(parentSchema);
                while (in.hasNext()) {
                    final var jsonElementName = in.nextName();
                    final var namespaceAndName = resolveNamespace(schemaCache, parentSchema, jsonElementName);
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

                    final var childDataSchemaNodes = schemaCache.findSchemaNode(parentSchema, localName,
                        getCurrentNamespace());
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

    private Entry<String, XMLNamespace> resolveNamespace(final SchemaNodeCache schemaCache,
            final DataSchemaNode dataSchemaNode, final String childName) {
        final int lastIndexOfColon = childName.lastIndexOf(':');
        final String nodeNamePart;
        XMLNamespace namespace;
        if (lastIndexOfColon != -1) {
            final var moduleNamePart = childName.substring(0, lastIndexOfColon);
            nodeNamePart = childName.substring(lastIndexOfColon + 1);

            final var m = codecs.modelContext().findModuleStatements(moduleNamePart).iterator();
            namespace = m.hasNext() ? m.next().localQNameModule().namespace() : null;
        } else {
            nodeNamePart = childName;
            namespace = null;
        }

        if (namespace == null) {
            final var potentialUris = schemaCache.potentialNamespaces(dataSchemaNode, nodeNamePart);
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
            sb.append(codecs.modelContext().findModuleStatements(potentialUri).iterator().next()
                .argument().getLocalName());
        }
        return sb.toString();
    }

    private static ImmutableSet<XMLNamespace> computePotentialNamespaces(final DataSchemaNode dataSchemaNode,
            final String elementName) {
        final var potentialUris = new HashSet<XMLNamespace>();
        collectPotentialNamespaces(potentialUris, dataSchemaNode, elementName);
        return ImmutableSet.copyOf(potentialUris);
    }

    private static void collectPotentialNamespaces(final Set<XMLNamespace> potentialUris,
            final DataSchemaNode dataSchemaNode, final String elementName) {
        if (dataSchemaNode instanceof DataNodeContainer container) {
            final var choices = new ArrayList<ChoiceSchemaNode>();
            for (var childSchemaNode : container.getChildNodes()) {
                if (childSchemaNode instanceof ChoiceSchemaNode choice) {
                    choices.add(choice);
                } else if (childSchemaNode.getQName().getLocalName().equals(elementName)) {
                    potentialUris.add(childSchemaNode.getQName().getNamespace());
                }
            }

            for (var choiceNode : choices) {
                for (var concreteCase : choiceNode.getCases()) {
                    collectPotentialNamespaces(potentialUris, concreteCase, elementName);
                }
            }
        }
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
