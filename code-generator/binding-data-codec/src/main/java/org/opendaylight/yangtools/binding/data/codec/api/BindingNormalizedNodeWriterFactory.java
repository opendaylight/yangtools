package org.opendaylight.yangtools.binding.data.codec.api;

import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

public interface BindingNormalizedNodeWriterFactory {

    public Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriter(final InstanceIdentifier<?> path,
            final NormalizedNodeStreamWriter domWriter);

    public BindingStreamEventWriter newWriterWithoutIdentifier(final InstanceIdentifier<?> path,
            final NormalizedNodeStreamWriter domWriter);

}
