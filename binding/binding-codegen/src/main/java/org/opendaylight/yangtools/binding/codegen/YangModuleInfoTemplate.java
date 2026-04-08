/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.contract.Naming.MODEL_BINDING_PROVIDER_CLASS_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.MODULE_INFO_CLASS_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.MODULE_INFO_INSTANCE_FIELD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.MODULE_INFO_QNAMEOF_METHOD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.MODULE_INFO_YANGDATANAMEOF_METHOD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.getClassName;
import static org.opendaylight.yangtools.binding.contract.Naming.getServicePackageName;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.RevisionUnion;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.api.Submodule;

/**
 * Template for {@link YangModuleInfo} implementation for a particular module. Aside from fulfilling that contract,
 * this class provides a static {@code createQName(String)} method, which is used by co-generated code to initialize
 * QNAME constants.
 */
public final class YangModuleInfoTemplate {
    // These are always imported. Note we need to import even java.lang members, as there can be conflicting definitions
    // in our package
    private static final String CORE_IMPORT_STR = """
        import com.google.common.collect.ImmutableSet;
        import java.lang.Override;
        import java.lang.String;
        import org.eclipse.jdt.annotation.NonNull;
        import org.opendaylight.yangtools.binding.lib.ResourceYangModuleInfo;
        import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
        import org.opendaylight.yangtools.yang.common.QName;
        """;

    private static final String EXT_IMPORT_STR = """
        import com.google.common.collect.ImmutableSet;
        import java.lang.Override;
        import java.lang.String;
        import java.util.HashSet;
        import java.util.Set;
        import org.eclipse.jdt.annotation.NonNull;
        import org.opendaylight.yangtools.binding.lib.ResourceYangModuleInfo;
        import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
        import org.opendaylight.yangtools.yang.common.QName;
        """;

    private final @NonNull Function<ModuleLike, List<String>> moduleFilePathResolver;
    private final @NonNull EffectiveModelContext modelContext;
    private final @NonNull Module module;
    private final @NonNull String packageName;
    private final @NonNull String modelBindingProviderName;
    private final boolean hasYangData;

    private String importedTypes = CORE_IMPORT_STR;

    public YangModuleInfoTemplate(final Module module, final EffectiveModelContext modelContext,
            final Function<ModuleLike, List<String>> moduleFilePathResolver) {
        this.module = requireNonNull(module);
        this.modelContext = requireNonNull(modelContext);
        this.moduleFilePathResolver = requireNonNull(moduleFilePathResolver);
        packageName = getServicePackageName(module.getQNameModule());
        modelBindingProviderName = packageName + '.' + MODEL_BINDING_PROVIDER_CLASS_NAME;
        hasYangData = module.asEffectiveStatement().findFirstEffectiveSubstatement(YangDataEffectiveStatement.class)
            .isPresent();
    }

    @NonNull String modelBindingProviderName() {
        return modelBindingProviderName;
    }

    @NonNull String packageName() {
        return packageName;
    }

    public String generate() {
        final var submodules = new LinkedHashSet<Submodule>();
        collectSubmodules(submodules, module);

        var bb = Block.builder()
            .eol("/**")
            .str(" * The {@link ResourceYangModuleInfo} for {@code ").str(module.getName()).eol("} module.")
            .eol(" */")
            .eol("@javax.annotation.processing.Generated(\"mdsal-binding-generator\")")
            .str("public final class " + MODULE_INFO_CLASS_NAME + " extends ResourceYangModuleInfo").oB()
                .str("private static final @NonNull QName NAME = QName.create(")
                    .jStr(module.getQNameModule().namespace().toString()).str(", ");
        module.getRevision().ifPresent(revision -> bb.jStr(revision.toString()).str(", "));
        bb
            .jStr(module.getName()).eol(").intern();")
            .txt("""

                  /**
                   * The singleton instance.
                   */
                  """)
            .eol("public static final @NonNull YangModuleInfo " + MODULE_INFO_INSTANCE_FIELD_NAME + " = new "
                + MODULE_INFO_CLASS_NAME + "();")
            .nl()
            .eol("private final @NonNull ImmutableSet<YangModuleInfo> importedModules;")
            .nl()
            .blk(classBody(module, MODULE_INFO_CLASS_NAME, submodules))
            .nl()
            .txt("""
                  /**
                   * Create an interned {@link QName} with specified {@code localName} and namespace/revision of this
                   * module.
                   *
                   * @param localName local name
                   * @return A QName
                   * @throws NullPointerException if {@code localName} is {@code null}
                   * @throws IllegalArgumentException if {@code localName} is not a valid YANG identifier
                   */
                  """)
            .str("public static @NonNull QName " + MODULE_INFO_QNAMEOF_METHOD_NAME + "(final String localName)").oB()
                .eol("return QName.create(NAME, localName).intern();")
            .cB();

        if (hasYangData) {
            bb
                .txt("""

                        /**
                         * Create an interned {@link YangDataName} with specified {@code templateName} and \
                    namespace/revision of
                         * this module.
                         *
                         * @param templateName template name
                         * @return A YangDataName
                         * @throws NullPointerException if {@code templateName} is {@code null}
                         * @throws IllegalArgumentException if {@code templateName} is empty
                         */
                     """)
                .str("    public static @NonNull YangDataName " + MODULE_INFO_YANGDATANAMEOF_METHOD_NAME
                    + "(final String templateName)").oB()
                .eol("        return new YangDataName(NAME.getModule(), templateName).intern();")
                .str("    ").cB();
        }
        final var body = bb.cB().toRawString();

        final var sb = new StringBuilder()
            .append("package ").append(packageName).append(";\n")
            .append('\n')
            .append(importedTypes);
        if (hasYangData) {
            sb.append("import org.opendaylight.yangtools.yang.common.YangDataName;\n");
        }
        return sb
            .append('\n')
            .append(body)
            .toString();
    }

    public String generateModelProvider() {
        return "package " + packageName + ";\n"
            +  """

                import java.lang.Override;
                import java.util.ServiceLoader;
                import org.opendaylight.yangtools.binding.meta.YangModelBindingProvider;
                import org.opendaylight.yangtools.binding.meta.YangModuleInfo;

                /**
                """
            +  " * The {@link YangModelBindingProvider} for {@code " + module.getName()
                + "} module. This class should not be used\n"
            +  " * directly, but rather through {@link ServiceLoader}.\n"
            +  " */\n"
            +  '@' + JavaFileTemplate.GENERATED + "(\"mdsal-binding-generator\")\n"
            +  "public final class " + MODEL_BINDING_PROVIDER_CLASS_NAME + " implements YangModelBindingProvider {\n"
            +  """
                    /**
                     * Construct a new provider.
                     */
                """
            +  "    public " + MODEL_BINDING_PROVIDER_CLASS_NAME + "() {\n"
            +  "        // Nothing else\n"
            +  "    }\n"
            +  '\n'
            +  "    @Override\n"
            +  "    public YangModuleInfo getModuleInfo() {\n"
            +  "        return " + MODULE_INFO_CLASS_NAME + ".INSTANCE;\n"
            +  "    }\n"
            +  "}\n";
    }

    private static void collectSubmodules(final LinkedHashSet<Submodule> dest, final ModuleLike module) {
        for (var submodule : module.getSubmodules()) {
            if (dest.add(submodule)) {
                collectSubmodules(dest, submodule);
            }
        }
    }

    @NonNullByDefault
    private BlockBuilder classBody(final ModuleLike mod, final String className, final Set<Submodule> submodules) {
        final var bb = Block.builder()
            .str("private ").str(className).str("()").oB();

        if (!mod.getImports().isEmpty() || !submodules.isEmpty()) {
            importedTypes = YangModuleInfoTemplate.EXT_IMPORT_STR;
            bb.eol("Set<YangModuleInfo> set = new HashSet<>();");
        }

        for (var imp : mod.getImports()) {
            final var name = imp.getModuleName().getLocalName();
            final var optRrev = imp.getRevision();

            final QNameModule qnameModule;
            if (optRrev.isEmpty()) {
                final var sorted = new TreeMap<RevisionUnion, Module>();
                for (var m : modelContext.getModules()) {
                    if (name.equals(m.getName())) {
                        sorted.put(m.getQNameModule().revisionUnion(), m);
                    }
                }
                qnameModule = sorted.lastEntry().getValue().getQNameModule();
            } else {
                qnameModule = modelContext.findModule(name, optRrev).orElseThrow().getQNameModule();
            }

            bb.str("set.add(").str(getServicePackageName(qnameModule))
                .eol('.' + MODULE_INFO_CLASS_NAME + ".INSTANCE);");
        }

        for (var submodule : submodules) {
            bb.str("set.add(").str(getClassName(submodule.getName())).eol("Info.INSTANCE);");
        }

        if (mod.getImports().isEmpty() && submodules.isEmpty()) {
            bb.eol("importedModules = ImmutableSet.of();");
        } else {
            bb.eol("importedModules = ImmutableSet.copyOf(set);");
        }

        final var pathItems = moduleFilePathResolver.apply(mod);
        if (pathItems.isEmpty()) {
            throw new IllegalStateException("Module " + mod + " does not have a file path");
        }

        bb
            .cB()
            .txt("""

                  @Override
                  public QName getName() {
                      return NAME;
                  }

                  @Override
                  protected String resourceName() {
                  """)
                .str("    return \"");
        for (var pathItem : pathItems) {
            bb.str("/").str(pathItem);
        }
        bb
            .eol("\";")
            .txt("""
                  }

                  @Override
                  public ImmutableSet<YangModuleInfo> getImportedModules() {
                      return importedModules;
                  }
                  """);

        for (var sub : submodules) {
            final var subName = getClassName(sub.getName());

            bb
                .nl()
                .str("private static final class ").str(subName).str("Info extends ResourceYangModuleInfo").oB()
                    .str("private final @NonNull QName NAME = QName.create(")
                        .jStr(sub.getQNameModule().namespace().toString()).str(", ");
            sub.getRevision().ifPresent(rev -> bb.jStr(rev.toString()).str(", "));
            bb
                .jStr(sub.getName()).eol(").intern();")
                .nl()
                .str("static final @NonNull YangModuleInfo INSTANCE = new ").str(subName).eol("Info();")
                .nl()
                .eol("private final @NonNull ImmutableSet<YangModuleInfo> importedModules;")
                .nl()
                .blk(classBody(sub, subName + "Info", Set.of()))
                .cB();
        }

        return bb;
    }
}
