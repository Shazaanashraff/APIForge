package io.github.shazaanashraff.apiforge.modules.codegenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Serialises a {@link CodeGenerationResult} into a ZIP archive. */
public class TestFileWriter {

  public static byte[] toZip(CodeGenerationResult result) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(baos)) {
      for (GeneratedFile file : result.files()) {
        zip.putNextEntry(new ZipEntry(file.name()));
        zip.write(file.content().getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
      }
    }
    return baos.toByteArray();
  }

  private TestFileWriter() {}
}
