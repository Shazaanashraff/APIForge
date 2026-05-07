package io.github.shazaanashraff.apiforge.modules.codegenerator;

/** A single file produced by a code renderer (name = relative path, content = file text). */
public record GeneratedFile(String name, String content) {}
