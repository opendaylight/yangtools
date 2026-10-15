/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.toIdentityString;

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
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.util.AbstractNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.AnyXmlNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema.ChildReusePolicy;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.Composite;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.data.util.LeafListNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ListNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.MultipleEntryDataWithSchema;
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

/**
 * This class parses JSON elements from a GSON JsonReader. It disallows multiple elements of the same name unlike the
 * default GSON JsonParser.
 */
public final class JsonParserStream implements Closeable, Flushable {
    static final String ANYXML_ARRAY_ELEMENT_ID = "array-element";

    private static final Logger LOG = LoggerFactory.getLogger(JsonParserStream.class);
    // The module of each JSON member we are inside of, innermost first
    private final Deque<QNameModule> modules = new ArrayDeque<>();
    private final NormalizedNodeStreamWriter writer;
    private final JSONCodecFactory codecs;
    private final DataSchemaNode parentNode;
    private final @Nullable DataSchemaContext parentContext;

    private final SchemaInferenceStack stack;

    // TODO: consider class specialization to remove this field
    private final boolean lenient;

    private JsonParserStream(final NormalizedNodeStreamWriter writer, final JSONCodecFactory codecs,
            final SchemaInferenceStack stack, final boolean lenient) {
        this.writer = requireNonNull(writer);
        this.codecs = requireNonNull(codecs);
        this.stack = requireNonNull(stack);
        this.lenient = lenient;

        final var inferenceContext = stack.modelContext();
        if (!inferenceContext.equals(codecs.modelContext())) {
            throw new IllegalArgumentException("Mismatched inference: expecting model context %s, got %s"
                .formatted(toIdentityString(codecs.modelContext()), toIdentityString(inferenceContext)));
        }
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
        parentContext = codecs.dataContextTree().childByInference(stack.toInference());
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
     * @param parentNode Logical root node, which has to be rooted in {@code codecFactory}'s model context
     * @return A new {@link JsonParserStream}
     * @throws NullPointerException if any of the arguments are null
     * @throws IllegalArgumentException if {@code parentNode} comes from a different model context than
     *                                  {@code codecFactory}
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
     * @param parentNode Logical root node, which has to be rooted in {@code codecFactory}'s model context
     * @return A new {@link JsonParserStream}
     * @throws NullPointerException if any of the arguments are null
     * @throws IllegalArgumentException if {@code parentNode} comes from a different model context than
     *                                  {@code codecFactory}
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
            // FIXME: verify just to fool ErrorProne, but we really should be doing something smarter
            verifyNotNull(reader.peek());
            isEmpty = false;
            // FIXME: this has a special-case bypass for SchemaContext, where we end up emitting just the child while
            //        the usual of() would result in SchemaContext.NAME being the root
            final var compositeNodeDataWithSchema = new CompositeNodeDataWithSchema<>(parentNode);
            // If the parent is a list, its children are the children of its entries
            read(reader, compositeNodeDataWithSchema, DataSchemaContextTree.entryOf(parentContext));
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
            case NUMBER, STRING -> parentElement.appendChild(doc.createTextNode(in.nextString()));
            case BOOLEAN -> parentElement.appendChild(doc.createTextNode(Boolean.toString(in.nextBoolean())));
            case NULL -> {
                in.nextNull();
                parentElement.appendChild(doc.createTextNode("null"));
            }
            case BEGIN_ARRAY -> {
                in.beginArray();
                while (in.hasNext()) {
                    final var childElement = doc.createElement(ANYXML_ARRAY_ELEMENT_ID);
                    parentElement.appendChild(childElement);
                    traverseAnyXmlValue(in, doc, childElement);
                }
                in.endArray();
            }
            case BEGIN_OBJECT -> {
                in.beginObject();
                while (in.hasNext()) {
                    final var childElement = doc.createElement(in.nextName());
                    parentElement.appendChild(childElement);
                    traverseAnyXmlValue(in, doc, childElement);
                }
                in.endObject();
            }
            default -> {
                // ignored
            }
        }
    }

    private void readAnyXmlValue(final JsonReader in, final AnyXmlNodeDataWithSchema parent,
            final String anyXmlObjectName) throws IOException {
        final var doc = UntrustedXML.newDocumentBuilder().newDocument();
        final var rootElement = doc.createElementNS(getCurrentModule().namespace().toString(), anyXmlObjectName);
        doc.appendChild(rootElement);
        traverseAnyXmlValue(in, doc, rootElement);

        parent.setValue(new DOMSource(doc.getDocumentElement()));
    }

    private void read(final JsonReader in, AbstractNodeDataWithSchema<?> parent,
            @Nullable DataSchemaContext context) throws IOException {
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
                        read(in, parent, context);
                    } else {
                        read(in, newArrayEntry(parent), DataSchemaContextTree.entryOf(context));
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
                    context = DataSchemaContextTree.entryOf(context);
                }

                final var parentSchema = parent.getSchema();
                // Anything but a container or a list entry has no children to look up
                final var composite = context instanceof Composite found ? found : null;
                while (in.hasNext()) {
                    final var jsonElementName = in.nextName();
                    final var nameAndModule = resolveModule(jsonElementName, parentSchema, composite);
                    final var localName = nameAndModule.getKey();
                    final var module = nameAndModule.getValue();
                    if (lenient && (localName == null || module == null)) {
                        LOG.debug("Schema node with name {} was not found under {}", localName,
                            parentSchema.getQName());
                        in.skipValue();
                        continue;
                    }
                    addModule(module);
                    if (!namesakes.add(jsonElementName)) {
                        throw new JsonSyntaxException("Duplicate name " + jsonElementName + " in JSON input.");
                    }

                    final var qname = composite == null ? null : childQName(module, localName);
                    final var child = qname == null ? null
                        : ((CompositeNodeDataWithSchema<?>) parent).addChild(composite, qname, ChildReusePolicy.NOOP);
                    if (child == null) {
                        throw new IllegalStateException(
                            "Schema for node with name %s and namespace %s does not exist at %s".formatted(
                                localName, module.namespace(), parentSchema));
                    }

                    if (child.data() instanceof AnyXmlNodeDataWithSchema anyxml) {
                        readAnyXmlValue(in, anyxml, jsonElementName);
                    } else {
                        stack.enterDataTree(qname);
                        read(in, child.data(), child.context());
                        stack.exit();
                    }
                    removeModule();
                }
                in.endObject();
                return;
            default:
                break;
        }
    }

    // Returns null if localName is not a valid YANG identifier, so no schema node can have it
    private static @Nullable QName childQName(final QNameModule module, final String localName) {
        final var unqualified = Unqualified.tryLocalName(localName);
        return unqualified == null ? null : unqualified.bindTo(module);
    }

    // Returns true if the composite has a data node with this name in this module. The name of a choice does not
    // count, as it never names a JSON member.
    private static boolean hasDataChild(final Composite composite, final QNameModule module, final String localName) {
        final var qname = childQName(module, localName);
        return qname != null && DataSchemaContextTree.dataStep(composite, qname) != null;
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

    private void removeModule() {
        modules.pop();
    }

    private void addModule(final QNameModule module) {
        modules.push(module);
    }

    private Entry<String, QNameModule> resolveModule(final String childName, final DataSchemaNode dataSchemaNode,
            final @Nullable Composite composite) {
        final int lastIndexOfColon = childName.lastIndexOf(':');
        final String nodeNamePart;
        QNameModule module;
        if (lastIndexOfColon != -1) {
            final var moduleNamePart = childName.substring(0, lastIndexOfColon);
            nodeNamePart = childName.substring(lastIndexOfColon + 1);
            module = resolvePrefix(moduleNamePart, nodeNamePart, composite);
        } else {
            nodeNamePart = childName;
            module = null;
        }

        if (module == null) {
            // Usually the child is in the parent's module, which is a single lookup. Only otherwise do we scan the
            // children for any module holding such a name.
            final var current = getCurrentModule();
            if (composite != null && current != null && hasDataChild(composite, current, nodeNamePart)) {
                return new SimpleImmutableEntry<>(nodeNamePart, current);
            }

            final var potentialModules = resolveAllPotentialModules(nodeNamePart, dataSchemaNode);
            if (potentialModules.size() == 1) {
                module = potentialModules.iterator().next();
            } else if (potentialModules.size() > 1) {
                throw new IllegalStateException("Choose suitable module name for element " + nodeNamePart + ":"
                        + toModuleNames(potentialModules));
            } else if (potentialModules.isEmpty() && !lenient) {
                throw new IllegalStateException("Schema node with name " + nodeNamePart + " was not found under "
                        + dataSchemaNode.getQName() + ".");
            }
        }

        return new SimpleImmutableEntry<>(nodeNamePart, module);
    }

    // A JSON prefix is a module name without a revision, but the model may hold several revisions of that module. They
    // come newest first: we take the newest one, unless only an older one has a child with this name.
    private @Nullable QNameModule resolvePrefix(final String moduleName, final String localName,
            final @Nullable Composite composite) {
        final var it = codecs.modelContext().findModuleStatements(moduleName).iterator();
        if (!it.hasNext()) {
            return null;
        }
        final var newest = it.next().localQNameModule();
        // With a single revision there is nothing to choose from, so we do not look up the child here
        if (composite != null && it.hasNext() && !hasDataChild(composite, newest, localName)) {
            while (it.hasNext()) {
                final var older = it.next().localQNameModule();
                if (hasDataChild(composite, older, localName)) {
                    return older;
                }
            }
        }
        return newest;
    }

    private String toModuleNames(final Collection<QNameModule> potentialModules) {
        final var sb = new StringBuilder();
        for (var potentialModule : potentialModules) {
            sb.append('\n');
            // FIXME how to get information about revision from JSON input? currently first available is used.
            sb.append(codecs.modelContext().findModuleStatements(potentialModule.namespace()).iterator().next()
                .argument().getLocalName());
        }
        return sb.toString();
    }

    private static Collection<QNameModule> resolveAllPotentialModules(final String elementName,
            final DataSchemaNode dataSchemaNode) {
        // Several revisions of one module share a namespace and so look the same in JSON: count them once, keeping the
        // newest revision
        final var byNamespace = new HashMap<XMLNamespace, QNameModule>();
        collectPotentialModules(elementName, dataSchemaNode, byNamespace);
        return byNamespace.values();
    }

    private static void collectPotentialModules(final String elementName, final DataSchemaNode dataSchemaNode,
            final Map<XMLNamespace, QNameModule> byNamespace) {
        final var choices = new HashSet<ChoiceSchemaNode>();
        if (dataSchemaNode instanceof DataNodeContainer container) {
            for (var childSchemaNode : container.getChildNodes()) {
                if (childSchemaNode instanceof ChoiceSchemaNode choice) {
                    choices.add(choice);
                } else if (childSchemaNode.getQName().getLocalName().equals(elementName)) {
                    final var module = childSchemaNode.getQName().getModule();
                    byNamespace.merge(module.namespace(), module,
                        (prev, next) -> Revision.compare(prev.revision(), next.revision()) >= 0 ? prev : next);
                }
            }

            for (var choiceNode : choices) {
                for (var concreteCase : choiceNode.getCases()) {
                    collectPotentialModules(elementName, concreteCase, byNamespace);
                }
            }
        }
    }

    private QNameModule getCurrentModule() {
        return modules.peek();
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
