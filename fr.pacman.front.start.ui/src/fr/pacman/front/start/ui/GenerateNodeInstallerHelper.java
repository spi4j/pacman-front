package fr.pacman.front.start.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

/**
 * Helper pour vérifier l'installation de Node.js et npm, et installer les
 * dépendances nécessaires pour un projet React dans Eclipse.
 * <p>
 * Cette classe permet :
 * <ul>
 * <li>De vérifier que Node.js et npm sont disponibles sur la machine.</li>
 * <li>D'installer les dépendances npm essentielles pour un projet React (ex :
 * react-router-dom et ses types).</li>
 * <li>D'exclure le dossier <code>node_modules</code> de l'indexation Eclipse
 * pour améliorer les performances.</li>
 * </ul>
 * </p>
 * <p>
 * Cette classe est utilisée par le wizard de création de projet React/Vite afin
 * de préparer l'environnement Node/npm nécessaire au projet.
 * 
 * @author MINARM
 */
public class GenerateNodeInstallerHelper {

	/**
	 * Chemin vers l'exécutable Node.js
	 */
	private final String nodeExec;

	/**
	 * Chemin vers l'exécutable npm
	 */
	private final String npmExec;

	/**
	 * Constructeur privé. Utilisé en interne pour créer une instance avec les
	 * chemins Node/npm détectés.
	 * 
	 * @param nodeExec chemin vers Node.js
	 * @param npmExec  chemin vers npm
	 */
	private GenerateNodeInstallerHelper(String nodeExec, String npmExec) {
		this.nodeExec = nodeExec;
		this.npmExec = npmExec;
	}

	/**
	 * Vérifie que Node.js et npm sont installés, puis installe les dépendances npm
	 * du projet.
	 * 
	 * @param project    le projet Eclipse concerné
	 * @param projectDir le répertoire du projet sur le système de fichiers
	 * @param monitor    sous-moniteur Eclipse pour suivre la progression
	 * @return {@link IStatus#OK_STATUS} si tout s'est bien passé, ou un
	 *         {@link IStatus} d'erreur sinon
	 */
	public static IStatus ensureToolsReady(IProject project, Path projectDir, SubMonitor monitor) {
		String nodeExec = findNodeExecutable();
		String npmExec = findNpmExecutable();

		GenerateNodeInstallerHelper helper = new GenerateNodeInstallerHelper(nodeExec, npmExec);
		IStatus status = helper.ensureNodeAndNpmInstalled();
		if (!status.isOK())
			return status;

		status = installDependencies(project, projectDir, monitor);
		if (!status.isOK())
			return status;

		return Status.OK_STATUS;
	}

	/**
	 * Vérifie que Node.js et npm sont installés sur la machine.
	 * 
	 * @return {@link Status#OK_STATUS} si Node.js et npm sont trouvés, sinon
	 *         {@link IStatus} d'erreur
	 */
	private IStatus ensureNodeAndNpmInstalled() {
		if (!checkCommand(nodeExec, "--version")) {
			return new Status(IStatus.ERROR, "mon.plugin", "Node.js non trouvé. Installation manuelle requise.");
		}
		if (!checkCommand(npmExec, "--version")) {
			return new Status(IStatus.ERROR, "mon.plugin", "npm non trouvé. Installation manuelle requise.");
		}
		return Status.OK_STATUS;
	}

	/**
	 * Vérifie qu'une commande peut être exécutée correctement.
	 * 
	 * @param exec le binaire à exécuter (node ou npm)
	 * @param arg  l'argument à passer (ex : "--version")
	 * @return true si la commande retourne 0, false sinon
	 */
	private boolean checkCommand(String exec, String arg) {
		try {
			Process process = new ProcessBuilder(exec, arg).start();
			return process.waitFor() == 0;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Installe les dépendances npm essentielles pour un projet React.
	 * 
	 * @param project    le projet Eclipse concerné
	 * @param projectDir le chemin du projet
	 * @param monitor    sous-moniteur Eclipse pour suivre la progression
	 * @return {@link IStatus#OK_STATUS} si l'installation réussit, sinon un
	 *         {@link IStatus} d'erreur
	 */
	private static IStatus installDependencies(IProject project, Path projectDir, SubMonitor monitor) {
		try {
			monitor.setTaskName("Installation des dépendances npm... (peux prendre plusieurs minutes "
					+ " en fonction du cache et de la connexion Internet.)");

			runProcess(projectDir, npmExec(), "install", "react-router-dom", "--save-dev", "@types/react-router-dom");
			excludeNodeModules(project);

			return Status.OK_STATUS;
		} catch (Exception e) {
			return new Status(IStatus.ERROR, "mon.plugin", "Échec de l'installation des dépendances npm", e);
		}
	}

	/**
	 * Exécute un processus système dans le répertoire spécifié et enregistre sa
	 * sortie dans un fichier de log.
	 * <p>
	 * Cette méthode :
	 * <ul>
	 * <li>Lance le processus avec les arguments fournis.</li>
	 * <li>Lit les flux de sortie standard et d'erreur du processus.</li>
	 * <li>Concatène toutes les lignes de sortie dans un {@link StringBuilder}.</li>
	 * <li>Appelle la méthode {@link #writeLog(Path, String)} pour enregistrer la
	 * sortie dans un fichier au niveau du projet.</li>
	 * <li>Attend la fin du processus et lance une exception si le code de sortie
	 * est non nul.</li>
	 * </ul>
	 * </p>
	 *
	 * @param projectDir le chemin du projet Eclipse où le processus est exécuté et
	 *                   où le log sera écrit
	 * @param command    les arguments du processus à exécuter (par exemple "npm",
	 *                   "install")
	 * @throws IOException          si une erreur d'entrée/sortie survient lors du
	 *                              lancement du processus ou de la lecture de ses
	 *                              flux
	 * @throws InterruptedException si le thread est interrompu pendant l'attente de
	 *                              la fin du processus
	 * @throws RuntimeException     si le processus retourne un code de sortie
	 *                              différent de 0
	 */
	private static void runProcess(Path projectDir, String... command) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(projectDir.toFile());
		Process process = pb.start();
		StringBuilder logBuilder = new StringBuilder();
		try (BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
			String line;
			while ((line = outReader.readLine()) != null) {
				logBuilder.append(line).append(System.lineSeparator());
			}
			while ((line = errReader.readLine()) != null) {
				logBuilder.append(line).append(System.lineSeparator());
			}
		}
		int exit = process.waitFor();
		writeLog(projectDir, logBuilder.toString());
		if (exit != 0) {
			throw new RuntimeException("Le processus est en erreur avec le code de retour :  " + exit);
		}
	}

	/**
	 * Écrit un message dans un fichier .log situé à la racine du projet. Si le
	 * fichier n'existe pas, il est créé.
	 * 
	 * @param projectDir le répertoire racine du projet
	 * @param logMessage le message à écrire
	 */
	public static void writeLog(Path projectDir, String logMessage) {
		try {
			Path logFile = projectDir.resolve("npm-install.log");
			Files.writeString(logFile, logMessage + System.lineSeparator(), StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace(); // tu peux aussi logger dans Eclipse
		}
	}

	/**
	 * Exclut le dossier <code>node_modules</code> de l'indexation Eclipse.
	 * 
	 * @param project le projet Eclipse
	 * @throws Exception si une erreur survient lors de la modification de la nature
	 *                   du dossier
	 */
	@SuppressWarnings("deprecation")
	private static void excludeNodeModules(IProject project) throws Exception {
		IFolder folder = project.getFolder("node_modules");
		if (folder.exists()) {
			folder.setDerived(true); // marque comme derived pour Eclipse (exclu de l'indexation)
		}
	}

	/**
	 * Retourne le binaire npm en fonction du système d'exploitation.
	 * 
	 * @return "npm.cmd" sous Windows, "npm" sinon
	 */
	private static String findNpmExecutable() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("win") ? "npm.cmd" : "npm";
	}

	/**
	 * Retourne le binaire Node.js en fonction du système d'exploitation.
	 * 
	 * @return "node.exe" sous Windows, "node" sinon
	 */
	private static String findNodeExecutable() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("win") ? "node.exe" : "node";
	}

	/**
	 * Alias pour récupérer le binaire npm.
	 * 
	 * @return chemin vers l'exécutable npm
	 */
	private static String npmExec() {
		return findNpmExecutable();
	}
}
