package io.github.shazaanashraff.apiforge.modules.codegenerator;

import java.util.List;

/** Output from a code renderer: the format used and all generated files. */
public record CodeGenerationResult(CodeFormat format, List<GeneratedFile> files) {}
