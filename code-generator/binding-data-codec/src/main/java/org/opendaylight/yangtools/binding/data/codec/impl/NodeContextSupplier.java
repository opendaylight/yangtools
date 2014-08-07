package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Supplier;

public interface NodeContextSupplier extends Supplier<NodeCodecContext> {


    @Override
    public NodeCodecContext get();
}
