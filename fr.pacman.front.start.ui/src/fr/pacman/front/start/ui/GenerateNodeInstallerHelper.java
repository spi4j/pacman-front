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
import org.eclipse.core.runtime.SubMonitor;

import fr.pacman.front.core.ui.service.PlugInUtils;

public class GenerateNodeInstallerHelper {

    private static final String NODE_DOWNLOAD_URL = "https://nodejs.org/dist/v20.17.0/node-v20.17.0-x64.msi";

    private final String nodeExec;
    private final String npmExec;

    private GenerateNodeInstallerHelper(String nodeExec, String npmExec) {
        this.nodeExec = nodeExec;
        this.npmExec = npmExec;
    }

    /**
     * Vérifie Node/npm et installe React Router automatiquement.
     */
    public static IStatus ensureToolsReady(Path projectDir, SubMonitor monitor) {
        String nodeExec = findNodeExecutable();
        String npmExec = findNpmExecutable();

        GenerateNodeInstallerHelper helper = new GenerateNodeInstallerHelper(nodeExec, npmExec);

        // Vérifie Node et npm
        IStatus status = helper.ensureNodeAndNpmInstalled();
        if (!status.isOK()) {
            return status;
        }

        // Installe React Router
        status = installReactRouter(projectDir, monitor);
        if (!status.isOK()) {
            return status;
        }

        return Status.OK_STATUS;
    }

    // ---------- Détection Node/npm ----------

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
        } catch (Exception ignored) {}
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
        } catch (Exception ignored) {}
        return System.getProperty("os.name").toLowerCase().contains("win") ? "node.exe" : "node";
    }

    // ---------- Vérification ----------

    private IStatus ensureNodeAndNpmInstalled() {
        IStatus status = checkToolInstalled(nodeExec, "Node.js",
                "Installez Node.js depuis https://nodejs.org/en/download/");
        if (!status.isOK()) {
            status = installNodeJs();
            if (!status.isOK()) return status;

            // Vérifier après installation
            status = checkToolInstalled(nodeExec, "Node.js",
                    "Node.js semble toujours absent, installation manuelle requise.");
            if (!status.isOK()) return status;
        }

        status = checkToolInstalled(npmExec, "npm",
                "Installez Node.js (npm est fourni avec) depuis https://nodejs.org/en/download/");
        return status;
    }

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

    // ---------- Installation Node.js (Windows uniquement) ----------

    private IStatus installNodeJs() {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            PlugInUtils.displayInformation("Installation manuelle requise",
                    "L'installation automatique n'est supportée que sous Windows. Téléchargez Node.js depuis https://nodejs.org/en/download/");
            return Status.CANCEL_STATUS;
        }

        try {
            URL url = new URL(NODE_DOWNLOAD_URL);
            Path tempFile = Files.createTempFile("node-installer", ".msi");
            try (InputStream in = url.openStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

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

    // ---------- Installation React Router ----------

    public static IStatus installReactRouter(Path projectDir, SubMonitor monitor) {
        try {
            monitor.setTaskName("Installation de react-router-dom...");
            runProcess(projectDir, monitor, findNpmExecutable(), "install", "react-router-dom");

            monitor.setTaskName("Installation des types @types/react-router-dom...");
            runProcess(projectDir, monitor, findNpmExecutable(), "install", "--save-dev", "@types/react-router-dom");

            return Status.OK_STATUS;

        } catch (Exception e) {
            return new Status(IStatus.ERROR, "mon.plugin", "Échec de l'installation de React Router", e);
        }
    }

    // ---------- Méthode utilitaire pour exécuter un processus ----------

    private static void runProcess(Path projectDir, SubMonitor monitor, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(projectDir.toFile());
        Process process = pb.start();

        Thread tOut = createThread(process.getInputStream(), monitor);
        Thread tErr = createThread(process.getErrorStream(), monitor);

        tOut.start();
        tErr.start();

        int exit = process.waitFor();
        tOut.join();
        tErr.join();

        if (exit != 0) {
            throw new RuntimeException("Process failed with code " + exit);
        }
    }

    private static Thread createThread(InputStream stream, SubMonitor monitor) {
        return new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    monitor.subTask(line);
                    monitor.worked(1);
                }
            } catch (Exception ignored) {}
        });
    }
}
