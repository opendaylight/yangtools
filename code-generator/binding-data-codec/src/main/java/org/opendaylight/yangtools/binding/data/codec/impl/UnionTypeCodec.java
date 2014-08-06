package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

class UnionTypeCodec extends ReflectionBasedCodec {

    Constructor<?> charConstructor;
    ImmutableSet<ValueOptionContext> typeCodecs;


    public UnionTypeCodec(final Class<?> unionCls,final Set<ValueOptionContext> codecs) {
        super(unionCls);
        try {
            charConstructor = unionCls.getConstructor(char[].class);
            typeCodecs = ImmutableSet.copyOf(codecs);
        } catch (NoSuchMethodException | SecurityException e) {
           throw new IllegalStateException("Required constructor is not available.",e);
        }
    }

    @SuppressWarnings("rawtypes")
    static final Callable<UnionTypeCodec> loader(final Class<?> unionCls,final UnionTypeDefinition unionType, final Codec instanceIdentifier, final Codec identity) {
        return new Callable<UnionTypeCodec>() {

            @Override
            public UnionTypeCodec call() throws Exception {
                Set<ValueOptionContext> values = new HashSet<>();
                for(TypeDefinition<?> subtype : unionType.getTypes()) {
                    String methodName = "get" + BindingMapping.getClassName(subtype.getQName());
                    Method valueGetter = unionCls.getMethod(methodName);
                    Class<?> valueType = valueGetter.getReturnType();
                    SchemaUnawareCodec valueCodec = ValueTypeCodec.getCodecFor(valueType, subtype);
                    values.add(new ValueOptionContext(valueType,valueGetter, valueCodec));
                }
                return new UnionTypeCodec(unionCls, values);
            }
        };
    }

    @Override
    public Object deserialize(final Object input) {
        try {
            return charConstructor.newInstance((input.toString().toCharArray()));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Object serialize(final Object input) {
        if(input == null) {
            return null;
        }

        for(ValueOptionContext valCtx : typeCodecs) {
            Object domValue = valCtx.serialize(input);
            if(domValue != null) {
                return domValue;
            }
        }
        return null;
    }

    private static class ValueOptionContext {

        final Method getter;
        final Class<?> bindingType;
        final Codec<Object,Object> codec;

        ValueOptionContext(final Class valueType,final Method getter, final Codec<Object, Object> codec) {
            this.getter = getter;
            this.bindingType = valueType;
            this.codec = codec;
        }

        public Object serialize(final Object input) {
            Object baValue = getValueFrom(input);
            if(baValue != null) {
                return codec.serialize(baValue);
            }
            return null;
        }

        public Object getValueFrom(final Object input) {
            try {
                return getter.invoke(input);
            } catch (IllegalAccessException  | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public int hashCode() {
            return bindingType.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ValueOptionContext other = (ValueOptionContext) obj;
            return bindingType.equals(other.bindingType);
        }
    }

}
