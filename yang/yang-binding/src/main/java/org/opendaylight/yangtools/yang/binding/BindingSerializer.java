package org.opendaylight.yangtools.yang.binding;

public interface BindingSerializer<P,I> {
    P serialize(I input);
}
