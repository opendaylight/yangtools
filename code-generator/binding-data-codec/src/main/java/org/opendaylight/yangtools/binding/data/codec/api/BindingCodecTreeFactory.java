package org.opendaylight.yangtools.binding.data.codec.api;

import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;

public interface BindingCodecTreeFactory {

    /**
     *
     * Creates Binding Codec Tree for specified Binding runtime context.
     *
     * @param context
     * @return Binding Codec Tree for specified Binding runtime context.
     */
    BindingCodecTree create(BindingRuntimeContext context);

}
