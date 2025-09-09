package fr.pacman.front.start.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Utilitaire pour exécuter des commandes npm dans un projet React/Vite depuis
 * Eclipse.
 * <p>
 * Cette classe permet de lancer {@code npm install} et {@code npm run dev} dans
 * le répertoire du projet et de rediriger toute la sortie vers une console
 * Eclipse dédiée.
 * </p>
 * <p>
 * Toutes les commandes sont exécutées dans un {@link Job} Eclipse afin de ne
 * pas bloquer l'UI.
 * </p>
 * 
 * @author MINARM
 */
public class NpmLauncher {

	/**
	 * Nom de la console Eclipse utilisée pour afficher la sortie npm
	 */
	private static final String c_console_name = "NPM Output";

	/**
	 * Recherche ou crée une console Eclipse pour afficher la sortie des commandes
	 * npm.
	 *
	 * @return la console Eclipse pour npm
	 */
	private static MessageConsole findConsole() {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsole[] existing = plugin.getConsoleManager().getConsoles();
		for (IConsole c : existing) {
			if (c.getName().equals(c_console_name)) {
				return (MessageConsole) c;
			}
		}
		MessageConsole console = new MessageConsole(c_console_name, null);
		plugin.getConsoleManager().addConsoles(new IConsole[] { console });
		return console;
	}

	/**
	 * Lance npm install et npm run dev (optionnel) dans le répertoire du projet.
	 * 
	 * L'exécution se fait dans un {@link Job} Eclipse pour ne pas bloquer l'UI.
	 *
	 * @param p_projectDir Répertoire du projet dans lequel exécuter les commandes
	 *                     npm
	 * @param p_runDev     Si vrai, lance également {@code npm run dev} après
	 *                     {@code npm install}
	 * @param p_npmExec    Chemin ou nom de l'exécutable npm (ex. "npm.cmd" sur
	 *                     Windows)
	 */
	public static void launchNpm(File p_projectDir, boolean p_runDev, String p_npmExec) {
		Job npmJob = new Job("NPM install / run") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				MessageConsole console = findConsole();
				try (MessageConsoleStream out = console.newMessageStream()) {
					out.println("Lancement de npm install dans " + p_projectDir);
					executeCommand(p_projectDir, out, p_npmExec, "install");

					if (p_runDev) {
						out.println("Lancement de npm run dev");
						executeCommand(p_projectDir, out, p_npmExec, "run", "dev");
					}
				} catch (Exception e) {
					e.printStackTrace();
					return new Status(IStatus.ERROR, "fr.pacman.front.start.ui", "Erreur NPM", e);
				}
				return Status.OK_STATUS;
			}
		};
		npmJob.setUser(true);
		npmJob.schedule();
	}

	/**
	 * Exécute une commande système dans le répertoire spécifié et redirige la
	 * sortie vers la console Eclipse.
	 *
	 * @param p_workingDir Répertoire de travail pour la commande
	 * @param p_out        Flux vers lequel écrire la sortie standard de la commande
	 * @param p_command    Tableau représentant la commande et ses arguments
	 * @throws Exception si la commande échoue à s'exécuter
	 */
	private static void executeCommand(File p_workingDir, MessageConsoleStream p_out, String... p_command)
			throws Exception {
		ProcessBuilder pb = new ProcessBuilder(p_command);
		pb.directory(p_workingDir);
		pb.redirectErrorStream(true);
		Process process = pb.start();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				p_out.println(line);
			}
		}
		if (process.waitFor() != 0) {
			p_out.println("Echec de la commande npm : " + String.join(" ", p_command));
		} else {
			p_out.println("Réussite de la commande npm : " + String.join(" ", p_command));
		}
	}
}
