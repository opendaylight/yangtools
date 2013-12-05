package org.opendaylight.yangtools.concepts;

public interface Codec<P,I> extends Serializer<P, I>, Deserializer<I, P> {

    @Override
    public I deserialize(P input);
    
    @Override
    public P serialize(I input);
}
