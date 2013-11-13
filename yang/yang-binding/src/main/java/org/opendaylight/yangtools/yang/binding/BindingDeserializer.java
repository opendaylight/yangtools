package org.opendaylight.yangtools.yang.binding;

public interface BindingDeserializer<P,I> {

    P deserialize(I input);
}
