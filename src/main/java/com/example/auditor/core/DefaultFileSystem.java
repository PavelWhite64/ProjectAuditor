package com.example.auditor.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/** Реализация FileSystem по умолчанию, использующая стандартные Java NIO. */
public class DefaultFileSystem implements FileSystem {

  @Override
  public String readFileContent(Path path) throws IOException {
    return Files.readString(path, StandardCharsets.UTF_8);
  }

  @Override
  public long getFileSize(Path path) throws IOException {
    return Files.size(path);
  }

  @Override
  public boolean exists(Path path) {
    return Files.exists(path);
  }

  @Override
  public boolean isDirectory(Path path) {
    return Files.isDirectory(path);
  }

  @Override
  public boolean isRegularFile(Path path) {
    return Files.isRegularFile(path);
  }

  @Override
  public List<Path> listFiles(Path dir) throws IOException {
    try (var stream = Files.list(dir)) {
      return stream.collect(Collectors.toList());
    }
  }

  @Override
  public void walkFileTree(Path start, FileVisitor visitor) throws IOException {
    Files.walkFileTree(
        start,
        new java.nio.file.FileVisitor<Path>() {
          @Override
          public java.nio.file.FileVisitResult preVisitDirectory(
              Path dir, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
            return convertResult(visitor.preVisitDirectory(dir));
          }

          @Override
          public java.nio.file.FileVisitResult visitFile(
              Path file, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
            return convertResult(visitor.visitFile(file));
          }

          @Override
          public java.nio.file.FileVisitResult visitFileFailed(Path file, IOException exc)
              throws IOException {
            return convertResult(visitor.visitFileFailed(file, exc));
          }

          @Override
          public java.nio.file.FileVisitResult postVisitDirectory(Path dir, IOException exc)
              throws IOException {
            return convertResult(visitor.postVisitDirectory(dir, exc));
          }

          private java.nio.file.FileVisitResult convertResult(FileVisitResult result) {
            switch (result) {
              case CONTINUE:
                return java.nio.file.FileVisitResult.CONTINUE;
              case TERMINATE:
                return java.nio.file.FileVisitResult.TERMINATE;
              case SKIP_SUBTREE:
                return java.nio.file.FileVisitResult.SKIP_SUBTREE;
              case SKIP_SIBLINGS:
                return java.nio.file.FileVisitResult.SKIP_SIBLINGS;
              default:
                return java.nio.file.FileVisitResult.CONTINUE;
            }
          }
        });
  }
}
