package fr.pacman.front.start.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import fr.pacman.front.core.ui.service.PlugInUtils;

public class NodeInstallerHelper {

    private static final String NODE_DOWNLOAD_URL = "https://nodejs.org/dist/v20.17.0/node-v20.17.0-x64.msi";

    private final String nodeExec;
    private final String npmExec;

    private NodeInstallerHelper(String nodeExec, String npmExec) {
        this.nodeExec = nodeExec;
        this.npmExec = npmExec;
    }

    /**
     * Méthode principale : vérifie et installe si nécessaire Node.js + npm
     */
    public static IStatus ensureToolsReady() {
        String nodeExec = findNodeExecutable();
        String npmExec = findNpmExecutable();

        NodeInstallerHelper helper = new NodeInstallerHelper(nodeExec, npmExec);
        return helper.ensureNodeAndNpmInstalled();
    }
    
    

    private IStatus ensureNodeAndNpmInstalled() {
        // Vérification Node.js
        IStatus status = checkToolInstalled(nodeExec, "Node.js",
                "Installez Node.js depuis https://nodejs.org/en/download/");
        if (!status.isOK()) {
            status = installNodeJs();
            if (!status.isOK()) {
                return status;
            }

            // Vérifier après install
            status = checkToolInstalled(nodeExec, "Node.js",
                    "Node.js semble toujours absent, installation manuelle requise.");
            if (!status.isOK()) {
                return status;
            }
        }

        // Vérification npm
        status = checkToolInstalled(npmExec, "npm",
                "Installez Node.js (npm est fourni avec) depuis https://nodejs.org/en/download/");
        return status;
    }

    // ---------- Détection ----------

    public static String findNpmExecutable() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process = os.contains("win")
                    ? new ProcessBuilder("where", "npm.cmd").start()
                    : new ProcessBuilder("which", "npm").start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String path = reader.readLine();
                if (path != null && !path.isBlank()) {
                    return path.trim();
                }
            }
        } catch (Exception ignored) {
        }

        // Fallback
        return System.getProperty("os.name").toLowerCase().contains("win") ? "npm.cmd" : "npm";
    }

    private static String findNodeExecutable() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process = os.contains("win")
                    ? new ProcessBuilder("where", "node.exe").start()
                    : new ProcessBuilder("which", "node").start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String path = reader.readLine();
                if (path != null && !path.isBlank()) {
                    return path.trim();
                }
            }
        } catch (Exception ignored) {
        }

        // Fallback
        return System.getProperty("os.name").toLowerCase().contains("win") ? "node.exe" : "node";
    }

    // ---------- Vérification ----------

    private IStatus checkToolInstalled(String exec, String toolName, String installMessage) {
        if (!checkCommand(exec, "--version")) {
            PlugInUtils.displayInformation(toolName + " non installé", installMessage);
            return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
    }

    private boolean checkCommand(String exec, String arg) {
        try {
            ProcessBuilder pb = new ProcessBuilder(exec, arg);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------- Installation auto (Windows uniquement) ----------

    private IStatus installNodeJs() {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            PlugInUtils.displayInformation("Installation manuelle requise",
                    "L'installation automatique n'est supportée que sous Windows. Téléchargez Node.js depuis https://nodejs.org/en/download/");
            return Status.CANCEL_STATUS;
        }

        try {
            // Téléchargement du MSI
            URL url = new URL(NODE_DOWNLOAD_URL);
            Path tempFile = Files.createTempFile("node-installer", ".msi");
            try (InputStream in = url.openStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Installation silencieuse
            ProcessBuilder pb = new ProcessBuilder("msiexec", "/i", tempFile.toString(), "/qn");
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                PlugInUtils.displayInformation("Installation réussie", "Node.js a été installé avec succès.");
                return Status.OK_STATUS;
            } else {
                return new Status(IStatus.ERROR, "mon.plugin",
                        "Échec de l'installation de Node.js (code " + exitCode + ")");
            }
        } catch (Exception e) {
            return new Status(IStatus.ERROR, "mon.plugin", "Erreur lors de l'installation de Node.js", e);
        }
    }
}
