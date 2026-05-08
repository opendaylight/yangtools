/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;

/**
 * A template for {@link Key} specializations.
 */
@NonNullByDefault
final class KeyTemplate extends ArchetypeTemplate<KeyArchetype> {
    record Builder(KeyArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public KeyTemplate build() {
            return new KeyTemplate(type, root);
        }
    }

    private static final JavaTypeName KEY = JavaTypeName.create(Key.class);

    private KeyTemplate(final KeyArchetype archetype, final DataRootArchetype root) {
        super(GeneratedClass.of(archetype), archetype, root);
    }

    @Override
    BlockBuilder body() {
        final var type = (KeyArchetype) type();
        final var typeName = type.simpleName();
        final var entryObject = importedName(type.entryObject());

        return newBlockBuilder()
            // FIXME: take advantage of javadocBlock() to add a module reference and a snippet
            .eol("/**")
            .str(" * This class represents the key of {@link ").str(entryObject).eol("} class.")
            .eol(" *")
            .str(" * @see ").eol(entryObject)
            .eol(" */")
            .blk(annotationDeclaration())
            .eol(generatedAnnotation())
            .str("public final class ").str(typeName).str(" implements ").gen(importedName(KEY), entryObject)
                .jBlock(this::classBody).nl();
    }

    // Split out to keep indentation in check
    private void classBody(final BlockBuilder bb) {
        final var type = (KeyArchetype) type();

        bb
            .eol("@java.io.Serial")
            .str("private static final long serialVersionUID = ").jLong(type.serialVersionUID()).eS()
            .newLine();

        // Fields
        // FIXME: generate checker methods for each property
        final var props = type.getProperties();
        for (var prop : props) {
            bb.str("private final ").str(importedNonNull(prop.getReturnType())).sp().str(fieldName(prop)).eS();
        }

        // All values constructor
        final var sortedProps = props.stream().sorted(PROP_COMPARATOR).collect(Collectors.toUnmodifiableList());
        bb
            .nl()
            .eol("/**")
            .eol(" * Constructs an instance.")
            .eol(" *");
        for (var prop : sortedProps) {
            bb.str(" * @param ").str(fieldName(prop)).str(" the entity ").eol(prop.getName());
        }
        bb
            .eol(" */")
            .str("public ").str(type.simpleName()).str("(").str(asNonNullArgumentsDeclaration(sortedProps)).str(")")
                .oB();
        for (var prop : sortedProps) {
            final var fieldName = fieldName(prop);
            bb.str("this.").str(fieldName).str(" = ").str(importedName(CODEHELPERS)).str(".requireKeyProp(")
                .str(fieldName).str(", ").jStr(prop.getName()).str(")").frg(cloneOrNull(prop)).eS();
            // FIXME: generate checker method invocation
        }
        bb.cB();

        final var it = props.iterator();
        do {
            final var field = it.next();
            final var fieldName = field.getName();
            final var returnType = field.getReturnType();

            bb
                .nl()
                // FIXME: emit a {@return .. } javadoc
                .eol("/**")
                .str(" * Return ").str(fieldName).eol(", guaranteed to be non-null.")
                .eol(" *")
                .str(" * @return {@code ").str(importedName(returnType)).str("} ").str(fieldName)
                .eol(", guaranteed to be non-null.")
                .eol(" */")
                // TODO: addComment(propBuilder, leaf) or as we should be able to look up the EbtryObjectArchetype and
                //       get the leaf from there: and then we do not need to store the types at all
                .str("public ").str(importedNonNull(returnType)).sp().str(getterMethodName(field)).str("()")
                .oB()
                .str("return ").str(fieldName(field)).frg(cloneOrNull(field)).eS()
                .cB();
        } while (it.hasNext());

        bb
            .nl()
            .blk(generateHashCode(props))
            .nl()
            .blk(generateEquals(props))
            .nl()
            .blk(generateToString(props));
    }
}
