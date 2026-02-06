/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;

/**
 * A {@code module} {@link SourceLinkage}.
 */
// FIXME: https://www.rfc-editor.org/rfc/rfc7950#section-7.1.6 on includes should be enforced for example -- somewhere
@NonNullByDefault
public record ModuleLinkage(
        ModuleRef ref,
        Unqualified name,
        YangVersion version,
        QNameModule namespace,
        Unqualified prefix,
        Map<Unqualified, ModuleRef> imports,
        Map<Unqualified, SubmoduleRef> includes) implements SourceLinkage {
    public ModuleLinkage {
        requireNonNull(ref);
        requireNonNull(name);
        requireNonNull(version);
        requireNonNull(namespace);
        requireNonNull(prefix);
        imports = Map.copyOf(imports);
        includes = Map.copyOf(includes);
    }
}