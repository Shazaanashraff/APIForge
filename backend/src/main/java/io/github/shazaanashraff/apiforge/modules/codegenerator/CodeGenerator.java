package io.github.shazaanashraff.apiforge.modules.codegenerator;

/** Strategy interface — one implementation per supported output format. */
interface CodeGenerator {
  CodeFormat format();

  CodeGenerationResult generate(CodeGenerationRequest request);
}
