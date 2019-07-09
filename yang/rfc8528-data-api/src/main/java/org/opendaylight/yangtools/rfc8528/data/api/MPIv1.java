package org.opendaylight.yangtools.rfc8528.data.api;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;

@NonNullByDefault
final class MPIv1 implements Serializable {
    private static final long serialVersionUID = 1L;

    private final QName qname;

    public MPIv1(final @NonNull QName qname) {
        this.qname = requireNonNull(qname);
    }

    private Object readResolve() {
        return MountPointIdentifier.create(qname);
    }
}
