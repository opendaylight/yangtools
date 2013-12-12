package org.opendaylight.yangtools.concepts;

public interface Deserializer<P,I> {

    P deserialize(I input);
}
