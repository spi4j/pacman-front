package fr.pacman.front.core.service;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class ZipUtils {

	public static void unzip(InputStream zipStream, Path targetDir) throws IOException {
		try (ZipInputStream zis = new ZipInputStream(zipStream)) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				Path newPath = targetDir.resolve(entry.getName()).normalize();

				if (entry.isDirectory()) {
					Files.createDirectories(newPath);
				} else {
					Files.createDirectories(newPath.getParent());
					try (OutputStream os = Files.newOutputStream(newPath, StandardOpenOption.CREATE,
							StandardOpenOption.TRUNCATE_EXISTING)) {
						zis.transferTo(os);
					}
				}
				zis.closeEntry();
			}
		}
	}
}
