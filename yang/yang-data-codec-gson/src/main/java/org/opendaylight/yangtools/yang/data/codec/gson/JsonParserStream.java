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
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;

import java.io.Closeable;
import java.io.EOFException;
import java.io.Flushable;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.helpers.IdentityValuesDTO;
import org.opendaylight.yangtools.yang.data.codec.gson.helpers.RestCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.helpers.RestUtil;
import org.opendaylight.yangtools.yang.data.codec.gson.helpers.RestUtil.PrefixMapingFromJson;
import org.opendaylight.yangtools.yang.data.codec.gson.helpers.SchemaContextUtils;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;

/**
 * This class parses JSON elements from a GSON JsonReader. It disallows multiple elements of the same name unlike the
 * default GSON JsonParser.
 */
@Beta
public final class JsonParserStream implements Closeable, Flushable {
    private final Deque<URI> namespaces = new ArrayDeque<>();
    private final NormalizedNodeStreamWriter writer;
    private final SchemaContextUtils utils;
    private final RestCodecFactory codecs;
    private final SchemaContext schema;

    private JsonParserStream(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext) {
        this.schema = Preconditions.checkNotNull(schemaContext);
        this.utils = SchemaContextUtils.create(schemaContext);
        this.writer = Preconditions.checkNotNull(writer);
        this.codecs = RestCodecFactory.create(utils);
    }

    public static JsonParserStream create(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext) {
        return new JsonParserStream(writer, schemaContext);
    }

    public JsonParserStream parse(final JsonReader reader) throws JsonIOException, JsonSyntaxException {
        // code copied from gson's JsonParser and Stream classes

        boolean lenient = reader.isLenient();
        reader.setLenient(true);
        boolean isEmpty = true;
        try {
            reader.peek();
            isEmpty = false;
            CompositeNodeDataWithSchema compositeNodeDataWithSchema = new CompositeNodeDataWithSchema(schema);
            read(reader, compositeNodeDataWithSchema);
            compositeNodeDataWithSchema.writeToStream(writer);

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

    public void read(final JsonReader in, final AbstractNodeDataWithSchema parent) throws IOException {

        final JsonToken peek = in.peek();
        Optional<String> value = Optional.absent();
        switch (peek) {
        case STRING:
        case NUMBER:
            value = Optional.of(in.nextString());
            break;
        case BOOLEAN:
            value = Optional.of(Boolean.toString(in.nextBoolean()));
            break;
        case NULL:
            in.nextNull();
            value = Optional.of((String) null);
            break;
        default:
            break;
        }
        if (value.isPresent()) {
            final Object translatedValue = translateValueByType(value.get(), parent.getSchema());
            ((SimpleNodeDataWithSchema) parent).setValue(translatedValue);
        }

        switch (peek) {
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
        }
    }

    private Object translateValueByType(final String value, final DataSchemaNode node) {
        final TypeDefinition<? extends Object> typeDefinition = typeDefinition(node);
        if (typeDefinition == null) {
            return value;
        }

        final Object inputValue;
        if (typeDefinition instanceof IdentityrefTypeDefinition) {
            inputValue = valueAsIdentityRef(value);
        } else if (typeDefinition instanceof InstanceIdentifierTypeDefinition) {
            inputValue = valueAsInstanceIdentifier(value);
        } else {
            inputValue = value;
        }

        return codecs.codecFor(typeDefinition).deserialize(inputValue);
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

    private static Object valueAsInstanceIdentifier(final String value) {
        // it could be instance-identifier Built-In Type
        if (!value.isEmpty() && value.charAt(0) == '/') {
            IdentityValuesDTO resolvedValue = RestUtil.asInstanceIdentifier(value, new PrefixMapingFromJson());
            if (resolvedValue != null) {
                return resolvedValue;
            }
        }
        throw new InvalidParameterException("Value for instance-identifier doesn't have correct format");
    }

    private static IdentityValuesDTO valueAsIdentityRef(final String value) {
        // it could be identityref Built-In Type
        URI namespace = getNamespaceFor(value);
        if (namespace != null) {
            return new IdentityValuesDTO(namespace.toString(), getLocalNameFor(value), null, value);
        }
        throw new InvalidParameterException("Value for identityref has to be in format moduleName:localName.");
    }

    private static URI getNamespaceFor(final String jsonElementName) {
        // The string needs to me in form "moduleName:localName"
        final int idx = jsonElementName.indexOf(':');
        if (idx == -1 || jsonElementName.indexOf(':', idx + 1) != -1) {
            return null;
        }

        // FIXME: is this correct? This should be looking up module name instead
        return URI.create(jsonElementName.substring(0, idx));
    }

    private static String getLocalNameFor(final String jsonElementName) {
        // The string needs to me in form "moduleName:localName"
        final int idx = jsonElementName.indexOf(':');
        if (idx == -1 || jsonElementName.indexOf(':', idx + 1) != -1) {
            return jsonElementName;
        }

        return jsonElementName.substring(idx + 1);
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
            namespace = utils.findNamespaceByModuleName(moduleNamePart);
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
     * Returns stack of schema nodes via which it was necessary to prass to get schema node with specified
     * {@code childName} and {@code namespace}
     *
     * @param dataSchemaNode
     * @param childName
     * @param namespace
     * @return stack of schema nodes via which it was passed through. If found schema node is dirrect child then stack
     *         contains only one node. If it is found under choice and case then stack should conains 2*n+1 element
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
