package org.opendaylight.yangtools.yang.binding;

public interface BindingCodec<P,I>  extends BindingSerializer<P, I>, BindingDeserializer<I, P> {

    @Override
    public P serialize(I input);
    
    @Override
    public I deserialize(P input);
}
