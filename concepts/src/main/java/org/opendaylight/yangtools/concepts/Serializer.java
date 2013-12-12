package org.opendaylight.yangtools.concepts;

public interface Serializer<P,I> {
    P serialize(I input);
}
