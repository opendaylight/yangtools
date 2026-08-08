/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.codegen.TypeNames.CODEHELPERS;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * A template for {@link Key} specializations.
 */
@NonNullByDefault
final class KeyTemplate extends ArchetypeTemplate<KeyArchetype> {
    private static final TypeName KEY = TypeName.ofClass(Key.class);
    // FIXME: ReturnType
    private static final ObjectEquality<Map.Entry<String, Type>> EQUALITY = new ObjectEquality<>(false) {
        @Override
        String fieldName(final Map.Entry<String, Type> obj) {
            return BaseTemplate.fieldName(obj.getKey());
        }

        @Override
        String propName(final Map.Entry<String, Type> obj) {
            return obj.getKey();
        }

        @Override
        boolean isBinaryType(final Map.Entry<String, Type> obj) {
            // FIXME: check for BaseYangTypes.BINARY_TYPE
            return obj.getValue().isArray();
        }
    };

    KeyTemplate(final DataRootArchetype root, final KeyArchetype archetype) {
        super(root, archetype);
    }

    @Override
    BlockBuilder body() {
        final var typeName = archetype.simpleName();
        final var entryObject = importedName(archetype.entryObject());

        return newBlockBuilder()
            // FIXME: take advantage of javadocBlock() to add a module reference and a snippet
            .eol("/**")
            .str(" * This class represents the key of {@link ").str(entryObject).eol("} class.")
            .eol(" *")
            .str(" * @see ").eol(entryObject)
            .eol(" */")
            .eol(generatedAnnotation())
            .frg(DeprecatedAnnotation.of(javaType(), archetype.entryObject().statement()))
            .str("public final class ").str(typeName).str(" implements ").gen(importedName(KEY), entryObject)
                .jBlock(this::classBody).nl();
    }

    // Split out to keep indentation in check
    private void classBody(final BlockBuilder bb) {
        bb
            .eol("@java.io.Serial")
            .str("private static final long serialVersionUID = ").jLong(serialVersionUID(archetype)).eS()
            .newLine();

        // Fields
        // FIXME: generate checker methods for each property
        final var props = archetype.methods().entrySet().stream()
            .map(entry -> Map.entry(Naming.getPropertyName(entry.getKey()), entry.getValue().returnType()))
            .toList();
        for (var entry : props) {
            bb.str("private final ").str(importedNonNull(entry.getValue())).str(" _").str(entry.getKey()).eS();
        }

        // All values constructor
        final var sortedProps = props.stream().sorted(Comparator.comparing(Map.Entry::getKey)).toList();
        bb
            .nl()
            .eol("/**")
            .eol(" * Constructs an instance.")
            .eol(" *");
        for (var prop : sortedProps) {
            bb.str(" * @param _").str(prop.getKey()).str(" the entity ").eol(prop.getKey());
        }
        bb
            .eol(" */")
            .str("public ").str(archetype.simpleName()).str("(").str(asNonNullArgumentsDeclaration(sortedProps))
                .str(")").oB();
        for (var prop : sortedProps) {
            bb.str("this._").str(prop.getKey()).str(" = ").str(importedName(CODEHELPERS)).str(".requireKeyProp(_")
                .str(prop.getKey()).str(", ").jStr(prop.getKey()).str(")").frg(cloneOrNull(prop.getValue())).eS();
            // FIXME: generate checker method invocation
        }
        bb.cB();

        final var it = props.iterator();
        do {
            final var field = it.next();
            final var fieldName = field.getKey();
            final var returnType = field.getValue();

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
                .str("public ").str(importedNonNull(returnType)).sp().str(getterMethodName(fieldName)).str("()").oB()
                    .str("return _").str(fieldName).frg(cloneOrNull(field.getValue())).eS()
                .cB();
        } while (it.hasNext());

        EQUALITY.append(bb, javaType(), props);
    }

    /**
     * Template method which generates method parameters with their types from <code>parameters</code>, annotating them
     * with {@link NonNull}.
     *
     * @param parameters group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    private String asNonNullArgumentsDeclaration(final List<Map.Entry<String, Type>> parameters) {
        final var it = parameters.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final var sb = new StringBuilder();
        while (true) {
            final var parameter = it.next();
            sb.append(importedNonNull(parameter.getValue())).append(" _").append(parameter.getKey());
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
    }

    /**
     * {@return the {@link BlockFragment} used to clone the property, or {@code null}}
     * @param type the type
     */
    private static @Nullable BlockFragment cloneOrNull(final Type type) {
        return type.isArray() ? bb -> bb.str(".clone()") : null;
    }

    private static long serialVersionUID(final KeyArchetype archetype) {
        final var svh = new SerialVersionHelper(archetype.name())
            .setAbstract(false)
            .addInterface(TypeName.ofClass(Key.class));
        for (var qname : archetype.statement().argument()) {
            svh.addField(Naming.getPropertyName(qname.getLocalName()));
        }
        return svh.computeSerialVersion();
    }
}
