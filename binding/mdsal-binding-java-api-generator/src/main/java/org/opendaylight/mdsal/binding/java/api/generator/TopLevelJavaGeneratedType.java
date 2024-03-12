/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;

/**
 * This class tracks types generated into a single Java compilation unit (file) and manages imports of that compilation
 * unit. Since we are generating only public classes, this is synonymous to a top-level Java type.
 */
@NonNullByDefault
final class TopLevelJavaGeneratedType extends AbstractJavaGeneratedType {
    private final BiMap<JavaTypeName, String> importedTypes = HashBiMap.create();

    TopLevelJavaGeneratedType(final GeneratedType genType) {
        super(genType);
    }

    @Override
    String localTypeName(final JavaTypeName type) {
        // Locally-anchored type, this is simple: just strip the first local name component and concat the others
        final Iterator<String> it = type.localNameComponents().iterator();
        it.next();

        final StringBuilder sb = new StringBuilder().append(it.next());
        while (it.hasNext()) {
            sb.append('.').append(it.next());
        }
        return sb.toString();
    }

    @Override
    boolean importCheckedType(final JavaTypeName type) {
        if (importedTypes.containsKey(type)) {
            return true;
        }
        final String simpleName = type.simpleName();
        if (importedTypes.containsValue(simpleName)) {
            return false;
        }
        importedTypes.put(type, simpleName);
        return true;
    }

    Stream<JavaTypeName> imports() {
        return importedTypes.entrySet().stream().filter(this::needsExplicitImport).map(Entry::getKey)
                .sorted(Comparator.comparing(JavaTypeName::toString));
    }

    private boolean needsExplicitImport(final Entry<JavaTypeName, String> entry) {
        final JavaTypeName name = entry.getKey();

        if (!getName().packageName().equals(name.packageName())) {
            // Different package: need to import it
            return true;
        }

        if (!name.immediatelyEnclosingClass().isPresent()) {
            // This a top-level class import, we can skip it
            return false;
        }

        // This is a nested class, we need to spell it out if the import entry points to the simple name
        return entry.getValue().equals(name.simpleName());
    }
}
