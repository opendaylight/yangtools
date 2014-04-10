/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT16_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT32_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT64_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT8_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT16_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT32_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT64_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT8_QNAME;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.data.api.codec.BinaryCodec;
import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.data.api.codec.DecimalCodec;
import org.opendaylight.yangtools.yang.data.api.codec.EmptyCodec;
import org.opendaylight.yangtools.yang.data.api.codec.EnumCodec;
import org.opendaylight.yangtools.yang.data.api.codec.Int16Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Int32Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Int64Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Int8Codec;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint16Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint32Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint64Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint8Codec;
import org.opendaylight.yangtools.yang.data.api.codec.UnionCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;
import org.w3c.dom.Element;

public abstract class TypeDefinitionAwareCodec<J, T extends TypeDefinition<T>> implements DataStringCodec<J> {

    private static final Pattern intPattern = Pattern.compile("[+-]?[1-9][0-9]*$");
    private static final Pattern hexPattern = Pattern.compile("[+-]?0[xX][0-9a-fA-F]+");
    private static final Pattern octalPattern = Pattern.compile("[+-]?0[1-7][0-7]*$");

    private final Optional<T> typeDefinition;
    private final Class<J> inputClass;

    private static final int provideBase(final String integer) {
        if (integer == null) {
            throw new IllegalArgumentException("String representing integer number cannot be NULL!");
        }

        if ((integer.length() == 1) && (integer.charAt(0) == '0')) {
            return 10;
        }

        final Matcher intMatcher = intPattern.matcher(integer);
        if (intMatcher.matches()) {
            return 10;
        } else {
            final Matcher hexMatcher = hexPattern.matcher(integer);
            if (hexMatcher.matches()) {
                return 16;
            } else {
                final Matcher octMatcher = octalPattern.matcher(integer);
                if (octMatcher.matches()) {
                    return 8;
                } else {
                    throw new NumberFormatException("Incorrect lexical representation of Integer value: " + integer
                            + "The Integer value can be defined as Integer Number, Hexadecimal Number or"
                            + "Octal Number. The sign vlues are allowed. "
                            + "Spaces between digits are NOT allowed!");
                }
            }
        }
    }

    private static String normalizeHexadecimal(final String hexInt) {
        if (hexInt == null) {
            throw new IllegalArgumentException(
                    "String representing integer number in Hexadecimal format cannot be NULL!");
        }
        final String normalizedString;
        if (hexInt.contains("x")) {
            normalizedString = hexInt.replace("x", "");
        } else if (hexInt.contains("X")) {
            normalizedString = hexInt.replace("X", "");
        } else {
            normalizedString = hexInt;
        }
        return normalizedString;
    }

    public static final BinaryCodecStringImpl BINARY_DEFAULT_CODEC = new BinaryCodecStringImpl(
            Optional.<BinaryTypeDefinition> absent());

    public static final BitsCodecStringImpl BITS_DEFAULT_CODEC = new BitsCodecStringImpl(
            Optional.<BitsTypeDefinition> absent());

    public static final BooleanCodecStringImpl BOOLEAN_DEFAULT_CODEC = new BooleanCodecStringImpl(
            Optional.<BooleanTypeDefinition> absent());

    public static final DecimalCodecStringImpl DECIMAL64_DEFAULT_CODEC = new DecimalCodecStringImpl(
            Optional.<DecimalTypeDefinition> absent());

    public static final EmptyCodecStringImpl EMPTY_DEFAULT_CODEC = new EmptyCodecStringImpl(
            Optional.<EmptyTypeDefinition> absent());

    public static final EnumCodecStringImpl ENUMERATION_DEFAULT_CODEC = new EnumCodecStringImpl(
            Optional.<EnumTypeDefinition> absent());

    public static final Int8CodecStringImpl INT8_DEFAULT_CODEC = new Int8CodecStringImpl(
            Optional.<IntegerTypeDefinition> absent());

    public static final Int16CodecStringImpl INT16_DEFAULT_CODEC = new Int16CodecStringImpl(
            Optional.<IntegerTypeDefinition> absent());

    public static final Int32CodecStringImpl INT32_DEFAULT_CODEC = new Int32CodecStringImpl(
            Optional.<IntegerTypeDefinition> absent());

    public static final Int64CodecStringImpl INT64_DEFAULT_CODEC = new Int64CodecStringImpl(
            Optional.<IntegerTypeDefinition> absent());

    public static final StringCodecStringImpl STRING_DEFAULT_CODEC = new StringCodecStringImpl(
            Optional.<StringTypeDefinition> absent());

    public static final Uint8CodecStringImpl UINT8_DEFAULT_CODEC = new Uint8CodecStringImpl(
            Optional.<UnsignedIntegerTypeDefinition> absent());

    public static final Uint16CodecStringImpl UINT16_DEFAULT_CODEC = new Uint16CodecStringImpl(
            Optional.<UnsignedIntegerTypeDefinition> absent());

    public static final Uint32CodecStringImpl UINT32_DEFAULT_CODEC = new Uint32CodecStringImpl(
            Optional.<UnsignedIntegerTypeDefinition> absent());

    public static final Uint64CodecStringImpl UINT64_DEFAULT_CODEC = new Uint64CodecStringImpl(
            Optional.<UnsignedIntegerTypeDefinition> absent());

    public static final UnionCodecStringImpl UNION_DEFAULT_CODEC = new UnionCodecStringImpl(
            Optional.<UnionTypeDefinition> absent());
    
    public static final InstanceIdentifierCodecImpl INSTANCE_IDENTIFIER_CODEC_IMPL = new InstanceIdentifierCodecImpl();

    public Class<J> getInputClass() {
        return inputClass;
    }

    protected TypeDefinitionAwareCodec(Optional<T> typeDefinition, Class<J> outputClass) {
        Preconditions.checkArgument(outputClass != null, "Output class must be specified.");
        this.typeDefinition = typeDefinition;
        this.inputClass = outputClass;
    }

    public Optional<T> getTypeDefinition() {
        return typeDefinition;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> from(TypeDefinition typeDefinition) {
        final TypeDefinitionAwareCodec codec = fromType(typeDefinition);
        return (TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>>) codec;
    }

    public static final <T extends TypeDefinition<T>> TypeDefinitionAwareCodec<?, T> fromType(T typeDefinition) {
        T superType = typeDefinition;
        while (superType.getBaseType() != null) {
            superType = superType.getBaseType();
        }

        @SuppressWarnings("rawtypes")
        TypeDefinitionAwareCodec codec = null;

        if (superType instanceof BinaryTypeDefinition) {
            codec = BINARY_DEFAULT_CODEC;
        } else if (superType instanceof BitsTypeDefinition) {
            codec = BITS_DEFAULT_CODEC;
        } else if (superType instanceof BooleanTypeDefinition) {
            codec = BOOLEAN_DEFAULT_CODEC;
        } else if (superType instanceof DecimalTypeDefinition) {
            codec = DECIMAL64_DEFAULT_CODEC;
        } else if (superType instanceof EmptyTypeDefinition) {
            codec = EMPTY_DEFAULT_CODEC;
        } else if (superType instanceof EnumTypeDefinition) {
            codec = ENUMERATION_DEFAULT_CODEC;
        } else if (superType instanceof IntegerTypeDefinition) {
            if (INT8_QNAME.equals(superType.getQName())) {
                codec = INT8_DEFAULT_CODEC;
            } else if (INT16_QNAME.equals(superType.getQName())) {
                codec = INT16_DEFAULT_CODEC;
            } else if (INT32_QNAME.equals(superType.getQName())) {
                codec = INT32_DEFAULT_CODEC;
            } else if (INT64_QNAME.equals(superType.getQName())) {
                codec = INT64_DEFAULT_CODEC;
            }
        } else if (superType instanceof StringTypeDefinition) {
            codec = STRING_DEFAULT_CODEC;
        } else if (superType instanceof UnionTypeDefinition) {
            codec = UNION_DEFAULT_CODEC;
        } else if (superType instanceof UnsignedIntegerTypeDefinition) {
            if (UINT8_QNAME.equals(superType.getQName())) {
                codec = UINT8_DEFAULT_CODEC;
            }
            if (UINT16_QNAME.equals(superType.getQName())) {
                codec = UINT16_DEFAULT_CODEC;
            }
            if (UINT32_QNAME.equals(superType.getQName())) {
                codec = UINT32_DEFAULT_CODEC;
            }
            if (UINT64_QNAME.equals(superType.getQName())) {
                codec = UINT64_DEFAULT_CODEC;
            }
        }
        @SuppressWarnings("unchecked")
        TypeDefinitionAwareCodec<?, T> ret = (TypeDefinitionAwareCodec<?, T>) codec;
        return ret;
    }

    public static class BooleanCodecStringImpl extends TypeDefinitionAwareCodec<Boolean, BooleanTypeDefinition>
            implements BooleanCodec<String> {

        protected BooleanCodecStringImpl(Optional<BooleanTypeDefinition> typeDef) {
            super(typeDef, Boolean.class);
        }

        @Override
        public String serialize(Boolean data) {
            return data == null ? "" : data.toString();
        }

        @Override
        public Boolean deserialize(String stringRepresentation) {
            return Boolean.valueOf(stringRepresentation);
        }
    };

    public static class Uint8CodecStringImpl extends TypeDefinitionAwareCodec<Short, UnsignedIntegerTypeDefinition>
            implements Uint8Codec<String> {

        protected Uint8CodecStringImpl(Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef, Short.class);
        }

        @Override
        public String serialize(Short data) {
            return data == null ? "" : data.toString();
        }

        @Override
        public Short deserialize(String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Short.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Short.valueOf(stringRepresentation, base);
        }
    };

    public static class Uint16CodecStringImpl extends TypeDefinitionAwareCodec<Integer, UnsignedIntegerTypeDefinition>
            implements Uint16Codec<String> {
        protected Uint16CodecStringImpl(Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef, Integer.class);
        }

        @Override
        public Integer deserialize(String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Integer.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Integer.valueOf(stringRepresentation, base);
        }

        @Override
        public String serialize(Integer data) {
            return data == null ? "" : data.toString();
        }
    };

    public static class Uint32CodecStringImpl extends TypeDefinitionAwareCodec<Long, UnsignedIntegerTypeDefinition>
            implements Uint32Codec<String> {

        protected Uint32CodecStringImpl(Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef, Long.class);
        }

        @Override
        public Long deserialize(String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Long.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Long.valueOf(stringRepresentation, base);
        }

        @Override
        public String serialize(Long data) {
            return data == null ? "" : data.toString();
        }
    };

    public static class Uint64CodecStringImpl extends
            TypeDefinitionAwareCodec<BigInteger, UnsignedIntegerTypeDefinition> implements Uint64Codec<String> {

        protected Uint64CodecStringImpl(Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef, BigInteger.class);
        }

        @Override
        public BigInteger deserialize(String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return new BigInteger(normalizeHexadecimal(stringRepresentation), base);
            }
            return new BigInteger(stringRepresentation, base);
        }

        @Override
        public String serialize(BigInteger data) {
            return data == null ? "" : data.toString();
        }
    };

    public static class StringCodecStringImpl extends TypeDefinitionAwareCodec<String, StringTypeDefinition> implements
            StringCodec<String> {

        protected StringCodecStringImpl(Optional<StringTypeDefinition> typeDef) {
            super(typeDef, String.class);
        }

        @Override
        public String deserialize(String stringRepresentation) {
            return stringRepresentation;
        }

        @Override
        public String serialize(String data) {
            return data == null ? "" : data.toString();
        }
    };

    public static class Int16CodecStringImpl extends TypeDefinitionAwareCodec<Short, IntegerTypeDefinition> implements
            Int16Codec<String> {

        protected Int16CodecStringImpl(Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef, Short.class);
        }

        @Override
        public Short deserialize(String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Short.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Short.valueOf(stringRepresentation, base);
        }

        @Override
        public String serialize(Short data) {
            return data == null ? "" : data.toString();
        }
    };

    public static class Int32CodecStringImpl extends TypeDefinitionAwareCodec<Integer, IntegerTypeDefinition> implements
            Int32Codec<String> {

        protected Int32CodecStringImpl(Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef, Integer.class);
        }

        @Override
        public Integer deserialize(String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Integer.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Integer.valueOf(stringRepresentation, base);
        }

        @Override
        public String serialize(Integer data) {
            return data == null ? "" : data.toString();
        }
    };

    public static class Int64CodecStringImpl extends TypeDefinitionAwareCodec<Long, IntegerTypeDefinition> implements
            Int64Codec<String> {

        protected Int64CodecStringImpl(Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef, Long.class);
        }

        @Override
        public Long deserialize(String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Long.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Long.valueOf(stringRepresentation, base);
        }

        @Override
        public String serialize(Long data) {
            return data == null ? "" : data.toString();
        }
    };

    public static class Int8CodecStringImpl extends TypeDefinitionAwareCodec<Byte, IntegerTypeDefinition> implements
            Int8Codec<String> {

        protected Int8CodecStringImpl(Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef, Byte.class);
        }

        @Override
        public Byte deserialize(String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Byte.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Byte.valueOf(stringRepresentation, base);
        }

        @Override
        public String serialize(Byte data) {
            return data == null ? "" : data.toString();
        }
    };

    public static class EmptyCodecStringImpl extends TypeDefinitionAwareCodec<Void, EmptyTypeDefinition> implements
            EmptyCodec<String> {

        protected EmptyCodecStringImpl(Optional<EmptyTypeDefinition> typeDef) {
            super(typeDef, Void.class);
        }

        @Override
        public String serialize(Void data) {
            return "";
        }

        @Override
        public Void deserialize(String stringRepresentation) {
            return null;
        }
    };

    public static final class BinaryCodecStringImpl extends TypeDefinitionAwareCodec<byte[], BinaryTypeDefinition>
            implements BinaryCodec<String> {

        protected BinaryCodecStringImpl(Optional<BinaryTypeDefinition> typeDef) {
            super(typeDef, byte[].class);
        }

        @Override
        public String serialize(byte[] data) {
            return data == null ? "" : BaseEncoding.base64().encode(data);
        }

        @Override
        public byte[] deserialize(String stringRepresentation) {
            return BaseEncoding.base64().decode(stringRepresentation);
        }
    };

    public static final class BitsCodecStringImpl extends TypeDefinitionAwareCodec<Set<String>, BitsTypeDefinition>
            implements BitsCodec<String> {

        public static final Joiner JOINER = Joiner.on(" ").skipNulls();
        public static final Splitter SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();

        @SuppressWarnings("unchecked")
        protected BitsCodecStringImpl(Optional<BitsTypeDefinition> typeDef) {
            super(typeDef, (Class<Set<String>>) ((Class<?>) Set.class));
        }

        @Override
        public String serialize(Set<String> data) {
            return data == null ? "" : JOINER.join(data);
        }

        @Override
        public Set<String> deserialize(String stringRepresentation) {
            if (stringRepresentation == null)
                return ImmutableSet.of();
            Iterable<String> strings = SPLITTER.split(stringRepresentation);
            return ImmutableSet.copyOf(strings);
        }
    };

    public static class EnumCodecStringImpl extends TypeDefinitionAwareCodec<String, EnumTypeDefinition> implements
            EnumCodec<String> {

        protected EnumCodecStringImpl(Optional<EnumTypeDefinition> typeDef) {
            super(typeDef, String.class);
        }

        @Override
        public String deserialize(String stringRepresentation) {
            return stringRepresentation;
        }

        @Override
        public String serialize(String data) {
            return data == null ? "" : data.toString();
        }
    };

    public static class DecimalCodecStringImpl extends TypeDefinitionAwareCodec<BigDecimal, DecimalTypeDefinition>
            implements DecimalCodec<String> {

        protected DecimalCodecStringImpl(Optional<DecimalTypeDefinition> typeDef) {
            super(typeDef, BigDecimal.class);
        }

        @Override
        public String serialize(BigDecimal data) {
            return data == null ? "" : data.toString();
        }

        @Override
        public BigDecimal deserialize(String stringRepresentation) {
            return new BigDecimal(stringRepresentation);
        }
    };

    public static class UnionCodecStringImpl extends TypeDefinitionAwareCodec<String, UnionTypeDefinition> implements
            UnionCodec<String> {

        protected UnionCodecStringImpl(Optional<UnionTypeDefinition> typeDef) {
            super(typeDef, String.class);
        }

        @Override
        public String serialize(String data) {
            return data == null ? "" : data;
        }

        @Override
        public String deserialize(String stringRepresentation) {
            return stringRepresentation;
        }
    };

    public static class InstanceIdentifierCodecImpl {

        private static final Pattern PREDICATE_PATTERN = Pattern.compile("\\[(.*?)\\]");
        public static final String SQUOTE = "'";
        public static final String DQUOTE = "\"";

        public InstanceIdentifierCodecImpl() {
        }

        public InstanceIdentifier deserialize(Element element,SchemaContext schemaContext) {
            Preconditions.checkNotNull(element, "Value of element for deserialization can't be null");
            Preconditions.checkNotNull(schemaContext, "Schema context for deserialization of instance identifier type can't be null");
            
            String valueTrimmed = element.getTextContent().trim();
            if (!valueTrimmed.startsWith("/")) {
                return null;
            }
            String[] xPathParts = valueTrimmed.split("/");
            if (xPathParts.length < 2) { // must be at least "/pr:node"
                return null;
            }
            List<PathArgument> result = new ArrayList<>();
            for (int i = 1; i < xPathParts.length; i++) {
                String xPathPartTrimmed = xPathParts[i].trim();

                PathArgument pathArgument = toPathArgument(xPathPartTrimmed,element,schemaContext);
                if (pathArgument != null) {
                    result.add(pathArgument);
                }
            }
            return new InstanceIdentifier(result);
        }
        
        public Element serialize(InstanceIdentifier data,Element element) {
            Preconditions.checkNotNull(data, "Variable should contain instance of instance identifier and can't be null" );
            Preconditions.checkNotNull(element, "DOM element can't be null" );            
            Map<String, String> prefixes = new HashMap<>();
            StringBuilder textContent = new StringBuilder();
            for (PathArgument pathArgument : data.getPath()) {
                textContent.append("/");
                writeIdentifierWithNamespacePrefix(element, textContent, pathArgument.getNodeType(), prefixes);
                if (pathArgument instanceof NodeIdentifierWithPredicates) {
                    Map<QName, Object> predicates = ((NodeIdentifierWithPredicates) pathArgument).getKeyValues();

                    for (QName keyValue : predicates.keySet()) {
                        String predicateValue = String.valueOf(predicates.get(keyValue));
                        textContent.append("[");
                        writeIdentifierWithNamespacePrefix(element, textContent, keyValue, prefixes);
                        textContent.append("='");
                        textContent.append(predicateValue);
                        textContent.append("'");
                        textContent.append("]");
                    }
                } else if (pathArgument instanceof NodeWithValue) {
                    textContent.append("[.='");
                    textContent.append(((NodeWithValue)pathArgument).getValue());
                    textContent.append("'");
                    textContent.append("]");
                }
            }
            element.setTextContent(textContent.toString());
            return element;
        }        
        

        private String getIdAndPrefixAsStr(String pathPart) {
            int predicateStartIndex = pathPart.indexOf("[");
            return predicateStartIndex == -1 ? pathPart : pathPart.substring(0, predicateStartIndex);
        }

        private PathArgument toPathArgument(String xPathArgument,Element element,SchemaContext schemaContext) {

            QName mainQName = toIdentity(xPathArgument,element,schemaContext);

            // predicates
            QName currentQName = mainQName;
            List<String> predicatesStr = new ArrayList<>();
            Map<QName, Object> predicates = new HashMap<>();
            Matcher matcher = PREDICATE_PATTERN.matcher(xPathArgument);
            while (matcher.find()) {
                predicatesStr.add(matcher.group(1).trim());
            }
            for (String predicateStr : predicatesStr) {
                int indexOfEqualityMark = predicateStr.indexOf("=");
                if (indexOfEqualityMark != -1) {
                    String predicateValue = toPredicateValue(predicateStr.substring(indexOfEqualityMark + 1));
                    if (predicateStr.startsWith(".")) { // it is leaf-list
                        if (predicateValue == null) {
                            return null;
                        }
                    } else {
                        currentQName = toIdentity(predicateStr.substring(0, indexOfEqualityMark),element,schemaContext);
                        if (currentQName == null || predicateValue == null) {
                            return null;
                        }
                    }
                    predicates.put(currentQName, predicateValue);
                }
            }

            if (predicates.isEmpty()) {
                return new InstanceIdentifier.NodeIdentifier(mainQName);
            } else {
                return new InstanceIdentifier.NodeIdentifierWithPredicates(mainQName, predicates);
            }

        }

        private QName toIdentity(String xPathArgument,Element element,SchemaContext schemaContext) {
            String xPathPart = getIdAndPrefixAsStr(xPathArgument);
            String xPathPartTrimmed = xPathPart.trim();
            if (xPathPartTrimmed.isEmpty()) {
                return null;
            }
            String[] prefixAndIdentifier = xPathPartTrimmed.split(":");
            // it is not "prefix:value"
            if (prefixAndIdentifier.length != 2) {
                return null;
            }
            String prefix = prefixAndIdentifier[0].trim();
            String identifier = prefixAndIdentifier[1].trim();
            if (prefix.isEmpty() || identifier.isEmpty()) {
                return null;
            }
            URI namespace = null;
            String namespaceStr = null;
            try {
                namespaceStr = element.lookupNamespaceURI(prefix);
                namespace = new URI(namespaceStr);
            } catch (URISyntaxException e) {
                new Exception("It wasn't possible to convert " + namespaceStr + " to URI object.");
            } catch (NullPointerException e) {
                new Exception("I wasn't possible to get namespace for prefix " + prefix);
            }
            Module youngestModule = findYoungestModuleByNamespace(schemaContext, namespace);

            return QName.create(namespace, youngestModule.getRevision(), identifier);
        }

        private Module findYoungestModuleByNamespace(SchemaContext schemaContext, URI namespace) {
            Module result = null;
            for (Module module : schemaContext.getModules()) {
                if (namespace.equals(module.getNamespace())) {
                    if (result != null) {
                        if (module.getRevision().after(result.getRevision())) {
                            result = module;
                        }
                    } else {
                        result = module;
                    }
                }
            }
            return result;
        }

        private static String toPredicateValue(String predicatedValue) {
            String predicatedValueTrimmed = predicatedValue.trim();
            if ((predicatedValueTrimmed.startsWith(DQUOTE) || predicatedValueTrimmed.startsWith(SQUOTE))
                    && (predicatedValueTrimmed.endsWith(DQUOTE) || predicatedValueTrimmed.endsWith(SQUOTE))) {
                return predicatedValueTrimmed.substring(1, predicatedValueTrimmed.length() - 1);
            }
            return null;
        }

        private static void writeIdentifierWithNamespacePrefix(Element element, StringBuilder textContent, QName qName,
                Map<String, String> prefixes) {
            String namespace = qName.getNamespace().toString();
            String prefix = prefixes.get(namespace);
            if (prefix == null) {
                prefix = qName.getPrefix();
                if (prefix == null || prefix.isEmpty() || prefixes.containsValue(prefix)) {
                    prefix = generateNewPrefix(prefixes.values());
                }
            }

            element.setAttribute("xmlns:" + prefix, namespace.toString());
            textContent.append(prefix);
            prefixes.put(namespace, prefix);

            textContent.append(":");
            textContent.append(qName.getLocalName());
        }
        
        private static String generateNewPrefix(Collection<String> prefixes) {
            StringBuilder result = null;
            Random random = new Random();
            do {
                result = new StringBuilder();
                for (int i = 0; i < 4; i++) {
                    int randomNumber = 0x61 + (Math.abs(random.nextInt()) % 26);
                    result.append(Character.toChars(randomNumber));
                }
            } while (prefixes.contains(result.toString()));

            return result.toString();
        }
    }
}
