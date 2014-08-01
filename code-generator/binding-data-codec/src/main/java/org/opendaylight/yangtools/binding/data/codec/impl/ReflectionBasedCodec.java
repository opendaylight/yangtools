package org.opendaylight.yangtools.binding.data.codec.impl;

abstract class ReflectionBasedCodec extends ValueTypeCodec {

    protected final Class<?> typeClass;

    public ReflectionBasedCodec(final Class<?> typeClass) {
        super();
        this.typeClass = typeClass;
    }
}