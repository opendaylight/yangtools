/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * This class parses JSON elements from a GSON JsonReader. It disallows multiple elements of the same name unlike the
 * default GSON JsonParser.
 */
@Beta
public final class JsonParserStream implements Closeable, Flushable {
    private final Deque<URI> namespaces = new ArrayDeque<>();
    private final NormalizedNodeStreamWriter writer;
    private final JSONCodecFactory codecs;
    private final SchemaContext schema;
    private final DataSchemaNode parentNode;

    private JsonParserStream(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext, final DataSchemaNode parentNode) {
        this.schema = Preconditions.checkNotNull(schemaContext);
        this.writer = Preconditions.checkNotNull(writer);
        this.codecs = JSONCodecFactory.create(schemaContext);
        this.parentNode = parentNode;
    }

    public static JsonParserStream create(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext, final SchemaNode parentNode ) {
        Preconditions.checkArgument(parentNode instanceof DataSchemaNode, "Instance of DataSchemaNode class awaited.");
        return new JsonParserStream(writer, schemaContext, (DataSchemaNode) parentNode);
    }

    public static JsonParserStream create(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext) {
        return new JsonParserStream(writer, schemaContext, schemaContext);
    }

    public JsonParserStream parse(final JsonReader reader) throws JsonIOException, JsonSyntaxException {
        // code copied from gson's JsonParser and Stream classes

        boolean lenient = reader.isLenient();
        reader.setLenient(true);
        boolean isEmpty = true;
        try {
            reader.peek();
            isEmpty = false;
            TopLevelNodeDataWithSchema topLevelNodeDataWithSchema = new TopLevelNodeDataWithSchema(parentNode);
            read(reader, topLevelNodeDataWithSchema);
            topLevelNodeDataWithSchema.normalizeTopLevelNode();
            topLevelNodeDataWithSchema.write(writer);
            return this;
            // return read(reader);
        } catch (EOFException e) {
            if (isEmpty) {
                return this;
                // return JsonNull.INSTANCE;
            }
            // The stream ended prematurely so it is likely a syntax error.
            throw new JsonSyntaxException(e);
        } catch (MalformedJsonException e) {
            throw new JsonSyntaxException(e);
        } catch (IOException e) {
            throw new JsonIOException(e);
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException(e);
        } catch (StackOverflowError | OutOfMemoryError e) {
            throw new JsonParseException("Failed parsing JSON source: " + reader + " to Json", e);
        } finally {
            reader.setLenient(lenient);
        }
    }

    private final void setValue(final AbstractNodeDataWithSchema parent, final String value) {
        Preconditions.checkArgument(parent instanceof SimpleNodeDataWithSchema, "Node %s is not a simple type", parent);

        final Object translatedValue = translateValueByType(value, parent.getSchema());
        ((SimpleNodeDataWithSchema) parent).setValue(translatedValue);
    }

    public void read(final JsonReader in, final AbstractNodeDataWithSchema parent) throws IOException {
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
                AbstractNodeDataWithSchema newChild = null;
                if (parent instanceof ListNodeDataWithSchema) {
                    newChild = new ListEntryNodeDataWithSchema(parent.getSchema());
                    ((CompositeNodeDataWithSchema) parent).addChild(newChild);
                } else if (parent instanceof LeafListNodeDataWithSchema) {
                    newChild = new LeafListEntryNodeDataWithSchema(parent.getSchema());
                    ((CompositeNodeDataWithSchema) parent).addChild(newChild);
                }
                read(in, newChild);
            }
            in.endArray();
            return;
        case BEGIN_OBJECT:
            Set<String> namesakes = new HashSet<>();
            in.beginObject();
            while (in.hasNext()) {
                final String jsonElementName = in.nextName();
                final NamespaceAndName namespaceAndName = resolveNamespace(jsonElementName);
                final String localName = namespaceAndName.getName();
                addNamespace(namespaceAndName.getUri());
                if (namesakes.contains(jsonElementName)) {
                    throw new JsonSyntaxException("Duplicate name " + jsonElementName + " in JSON input.");
                }
                namesakes.add(jsonElementName);
                final Deque<DataSchemaNode> childDataSchemaNodes = findSchemaNodeByNameAndNamespace(parent.getSchema(),
                        localName, getCurrentNamespace());
                if (childDataSchemaNodes.isEmpty()) {
                    throw new IllegalStateException("Schema for node with name " + localName + " and namespace "
                            + getCurrentNamespace() + " doesn't exist.");
                }

                AbstractNodeDataWithSchema newChild;
                newChild = ((CompositeNodeDataWithSchema) parent).addChild(childDataSchemaNodes);
//                FIXME:anyxml data shouldn't be skipped but should be loaded somehow. will be specified after 17AUG2014
                if (newChild instanceof AnyXmlNodeDataWithSchema) {
                    in.skipValue();
                } else {
                    read(in, newChild);
                }
                removeNamespace();
            }
            in.endObject();
            return;
        case END_DOCUMENT:
        case NAME:
        case END_OBJECT:
        case END_ARRAY:
            break;
        }
    }

    private Object translateValueByType(final String value, final DataSchemaNode node) {
        final TypeDefinition<? extends Object> typeDefinition = typeDefinition(node);
        if (typeDefinition == null) {
            return value;
        }

        return codecs.codecFor(typeDefinition).deserialize(value);
    }

    private static TypeDefinition<? extends Object> typeDefinition(final DataSchemaNode node) {
        TypeDefinition<?> baseType = null;
        if (node instanceof LeafListSchemaNode) {
            baseType = ((LeafListSchemaNode) node).getType();
        } else if (node instanceof LeafSchemaNode) {
            baseType = ((LeafSchemaNode) node).getType();
        } else if (node instanceof AnyXmlSchemaNode) {
            return null;
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " + Arrays.<Object> asList(node).toString());
        }

        if (baseType != null) {
            while (baseType.getBaseType() != null) {
                baseType = baseType.getBaseType();
            }
        }
        return baseType;
    }

    private void removeNamespace() {
        namespaces.pop();
    }

    private void addNamespace(final Optional<URI> namespace) {
        if (!namespace.isPresent()) {
            if (namespaces.isEmpty()) {
                throw new IllegalStateException("Namespace has to be specified at top level.");
            } else {
                namespaces.push(namespaces.peek());
            }
        } else {
            namespaces.push(namespace.get());
        }
    }

    private NamespaceAndName resolveNamespace(final String childName) {
        int lastIndexOfColon = childName.lastIndexOf(':');
        String moduleNamePart = null;
        String nodeNamePart = null;
        URI namespace = null;
        if (lastIndexOfColon != -1) {
            moduleNamePart = childName.substring(0, lastIndexOfColon);
            nodeNamePart = childName.substring(lastIndexOfColon + 1);

            final Module m = schema.findModuleByName(moduleNamePart, null);
            namespace = m == null ? null : m.getNamespace();
        } else {
            nodeNamePart = childName;
        }

        Optional<URI> namespaceOpt = namespace == null ? Optional.<URI> absent() : Optional.of(namespace);
        return new NamespaceAndName(nodeNamePart, namespaceOpt);
    }

    private URI getCurrentNamespace() {
        return namespaces.peek();
    }

    /**
     * Returns stack of schema nodes via which it was necessary to pass to get schema node with specified
     * {@code childName} and {@code namespace}
     *
     * @param dataSchemaNode
     * @param childName
     * @param namespace
     * @return stack of schema nodes via which it was passed through. If found schema node is direct child then stack
     *         contains only one node. If it is found under choice and case then stack should contains 2*n+1 element
     *         (where n is number of choices through it was passed)
     */
    private Deque<DataSchemaNode> findSchemaNodeByNameAndNamespace(final DataSchemaNode dataSchemaNode,
            final String childName, final URI namespace) {
        final Deque<DataSchemaNode> result = new ArrayDeque<>();
        List<ChoiceNode> childChoices = new ArrayList<>();
        if (dataSchemaNode instanceof DataNodeContainer) {
            for (DataSchemaNode childNode : ((DataNodeContainer) dataSchemaNode).getChildNodes()) {
                if (childNode instanceof ChoiceNode) {
                    childChoices.add((ChoiceNode) childNode);
                } else {
                    final QName childQName = childNode.getQName();
                    if (childQName.getLocalName().equals(childName) && childQName.getNamespace().equals(namespace)) {
                        result.push(childNode);
                        return result;
                    }
                }
            }
        }
        // try to find data schema node in choice (looking for first match)
        for (ChoiceNode choiceNode : childChoices) {
            for (ChoiceCaseNode concreteCase : choiceNode.getCases()) {
                Deque<DataSchemaNode> resultFromRecursion = findSchemaNodeByNameAndNamespace(concreteCase, childName,
                        namespace);
                if (!resultFromRecursion.isEmpty()) {
                    resultFromRecursion.push(concreteCase);
                    resultFromRecursion.push(choiceNode);
                    return resultFromRecursion;
                }
            }
        }
        return result;
    }

    private static class NamespaceAndName {
        private final Optional<URI> uri;
        private final String name;

        public NamespaceAndName(final String name, final Optional<URI> uri) {
            this.name = name;
            this.uri = uri;
        }

        public String getName() {
            return name;
        }

        public Optional<URI> getUri() {
            return uri;
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
