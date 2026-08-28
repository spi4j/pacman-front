
package fr.pacman.front.cinematic.react.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Gestionnaire du serveur de développement React utilisé par Pacman.
 * <p>
 * Cette classe permet de démarrer et d'arrêter le serveur de développement
 * React à partir de la commande {@code npm run dev}. Le processus npm est
 * exécuté dans le répertoire du projet React fourni en paramètre.
 * </p>
 * <p>
 * Les sorties standard et d'erreur du processus npm sont redirigées vers une
 * console dédiée dans Eclipse afin de permettre de suivre directement les
 * messages produits par npm, Vite et le serveur React. Le gestionnaire utilise
 * une instance unique afin que les différents handlers Pacman puissent accéder
 * au même processus serveur et notamment que le handler d'arrêt puisse arrêter
 * le processus précédemment démarré par le handler de démarrage.
 * </p>
 * 
 * @author MINARM
 */
public class ReactServerManager {

	/**
	 * Indique si l'arrêt du processus a été explicitement demandé par
	 * l'utilisateur.
	 * <p>
	 * Cet attribut est {@code volatile} car sa valeur peut être modifiée depuis le
	 * thread qui exécute le handler tandis qu'elle est consultée depuis le thread
	 * chargé de surveiller le processus npm.
	 * </p>
	 */
	private volatile boolean _stopping;

	/**
	 * Instance unique du gestionnaire du serveur React.
	 */
	private static final ReactServerManager c_instance = new ReactServerManager();

	/**
	 * Nom de la console Eclipse utilisée pour afficher les informations relatives
	 * au serveur React.
	 */
	private static final String c_consoleNname = "Pacman React Server";

	/**
	 * Processus npm actuellement exécuté.
	 * <p>
	 * Le processus correspond à la commande {@code npm run dev} lancée dans le
	 * répertoire du projet React.
	 * </p>
	 */
	private Process process;

	/**
	 * Constructeur privé afin d'empêcher la création directe d'instances de cette
	 * classe.
	 * <p>
	 * L'accès au gestionnaire s'effectue au moyen de la méthode
	 * {@link #getInstance()}.
	 * </p>
	 */
	private ReactServerManager() {
	}

	/**
	 * Retourne l'instance unique du gestionnaire du serveur React.
	 * 
	 * @return instance unique de {@link ReactServerManager}
	 */
	public static ReactServerManager getInstance() {
		return c_instance;
	}

	/**
	 * Démarre le serveur de développement React avec la commande
	 * {@code npm run dev}.
	 * <p>
	 * Avant de démarrer le processus, la méthode vérifie que le répertoire fourni
	 * existe.
	 * </p>
	 * 
	 * <p>
	 * Les sorties standard et d'erreur du processus npm sont redirigées vers la
	 * console Eclipse dédiée à Pacman. Un thread séparé est utilisé pour lire en
	 * continu les sorties du processus afin de ne pas bloquer le thread principal
	 * d'Eclipse.
	 * </p>
	 * 
	 * <p>
	 * Un second thread surveille la fin du processus npm afin de signaler une fin
	 * d'exécution qui n'aurait pas été provoquée par le handler d'arrêt.
	 * </p>
	 * 
	 * @param p_projectDirectory répertoire du projet React dans lequel la commande
	 *                           npm doit être exécutée @throws IOException si le
	 *                           répertoire du projet n'existe pas ou si le
	 *                           processus npm ne peut pas être démarré
	 */
	public void start(File p_projectDirectory) throws IOException {

		_stopping = false;
		final MessageConsole console = getConsole();
		if (process != null && process.isAlive()) {
			print(console, "Le serveur React est déjà démarré.");
			showConsole();
			return;
		}

		if (!p_projectDirectory.isDirectory()) {
			throw new IOException(
					"Le répertoire du projet React n'existe pas : " + p_projectDirectory.getAbsolutePath());
		}

		showConsole(console);
		print(console, "Démarrage du serveur React...");
		print(console, "Répertoire : " + p_projectDirectory.getAbsolutePath());
		print(console, "Commande : npm run dev");

		final ProcessBuilder processBuilder = new ProcessBuilder("npm.cmd", "run", "dev");
		processBuilder.directory(p_projectDirectory);
		processBuilder.redirectErrorStream(true);
		process = processBuilder.start();

		final Process currentProcess = process;

		/**
		 * Thread chargé de lire en continu les sorties produites par npm, * Node.js et
		 * Vite.
		 */
		final Thread outputThread = new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
					MessageConsoleStream stream = console.newMessageStream()) {
				String line;
				while ((line = reader.readLine()) != null) {
					stream.println(line);
				}
			} catch (IOException e) {
				print(console, "Erreur lors de la lecture des logs du serveur : " + e.getMessage());
			}
		}, "Pacman React Server Output");

		/**
		 * Thread chargé de surveiller la fin du processus npm. Si stopping vaut true,
		 * la fin du processus est la conséquence d'une demande explicite d'arrêt et le
		 * message n'est donc pas affiché ici.
		 */
		final Thread processWatcher = new Thread(() -> {
			try {
				final int exitCode = currentProcess.waitFor();
				if (!_stopping) {
					print(console, "Le processus npm s'est terminé. Code retour : " + exitCode);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}, "Pacman React Server Watcher");
		processWatcher.setDaemon(true);
		processWatcher.start();
		outputThread.setDaemon(true);
		outputThread.start();
		print(console, "Processus npm démarré.");
	}

	/**
	 * Arrête le serveur de développement React actuellement en cours d'exécution.
	 * <p>
	 * Sous Windows, la commande {@code taskkill} est utilisée avec les options
	 * {@code /T} et {@code /F} afin de terminer le processus npm ainsi que
	 * l'ensemble de ses processus enfants. Cette approche est nécessaire car
	 * {@code npm.cmd} peut lancer Node.js puis Vite comme processus enfants.
	 * </p>
	 * 
	 * <p>
	 * L'utilisation de {@code /T} permet notamment d'éviter que le processus Vite
	 * continue à occuper le port après l'arrêt de npm.
	 * </p>
	 * 
	 * <p>
	 * Si aucun serveur n'est actuellement démarré, un message d'information est
	 * affiché dans la console.
	 * </p>
	 */
	public void stop() {
		final MessageConsole console = getConsole();
		showConsole(console);
		if (process == null || !process.isAlive()) {
			print(console, "Aucun serveur React en cours d'exécution.");
			process = null;
			return;
		}

		/**
		 * Indique au thread de surveillance que la fin du processus résulte d'une
		 * demande explicite d'arrêt.
		 */
		_stopping = true;
		final long pid = process.pid();
		print(console, "Arrêt du serveur React...");
		print(console, "PID : " + pid);

		/**
		 * Sous Windows, /T permet d'arrêter également les processus enfants du
		 * processus npm et /F force leur terminaison.
		 */
		try {
			final Process killProcess = new ProcessBuilder("taskkill", "/PID", String.valueOf(pid), "/T", "/F")
					.redirectErrorStream(true).start();
			final int exitCode = killProcess.waitFor();
			if (exitCode == 0) {
				print(console, "Serveur React arrêté.");
			} else {
				print(console, "Impossible d'arrêter complètement le serveur React. " + "Code retour : " + exitCode);
			}
		} catch (IOException e) {
			print(console, "Erreur lors de l'arrêt du serveur React : " + e.getMessage());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			print(console, "Arrêt du serveur React interrompu.");
		}
		process = null;
	}

	/**
	 * Écrit un message dans la console Eclipse dédiée au serveur React.
	 * <p>
	 * Un flux temporaire est créé pour chaque message et automatiquement fermé à
	 * l'aide du mécanisme try-with-resources.
	 * </p>
	 * 
	 * @param p_console console Eclipse dans laquelle le message doit être écrit
	 * @param p_message message à afficher
	 */
	private void print(MessageConsole p_console, String p_message) {
		try (MessageConsoleStream stream = p_console.newMessageStream()) {
			stream.println(p_message);
		} catch (IOException e) {
			System.err.println("Impossible d'écrire dans la console Pacman React : " + e.getMessage());
		}
	}

	/**
	 * Affiche la console Pacman React dans la vue Console d'Eclipse.
	 * <p>
	 * L'accès aux composants graphiques SWT/JFace est effectué de manière
	 * asynchrone sur le thread graphique d'Eclipse.
	 * </p>
	 * 
	 * @param p_console console Pacman React à afficher
	 */
	private void showConsole(MessageConsole p_console) {
		Display.getDefault().asyncExec(() -> {
			try {
				final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				if (page == null) {
					return;
				}
				final IConsoleView view = (IConsoleView) page.showView("org.eclipse.ui.console.ConsoleView");
				view.display(p_console);
			} catch (PartInitException e) {
				System.err.println("Impossible d'afficher la console Pacman React : " + e.getMessage());
			}
		});
	}

	/**
	 * Affiche la console Pacman React dans la vue Console d'Eclipse.
	 * <p>
	 * Cette surcharge récupère automatiquement la console Pacman React avant de
	 * demander son affichage.
	 * </p>
	 */
	private void showConsole() {
		showConsole(getConsole());
	}

	/**
	 * Récupère la console Eclipse dédiée au serveur React.
	 * <p>
	 * Si une console portant le nom {@link #CONSOLE_NAME} existe déjà, celle-ci est
	 * réutilisée. Dans le cas contraire, une nouvelle {@link MessageConsole} est
	 * créée et enregistrée auprès du gestionnaire de consoles Eclipse.
	 * </p>
	 * 
	 * @return console Eclipse utilisée pour les messages du serveur React
	 */
	private MessageConsole getConsole() {
		final ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
		final var consoleManager = consolePlugin.getConsoleManager();
		for (IConsole console : consoleManager.getConsoles()) {
			if (c_consoleNname.equals(console.getName()) && console instanceof MessageConsole) {
				return (MessageConsole) console;
			}
		}
		final MessageConsole console = new MessageConsole(c_consoleNname, null);
		consoleManager.addConsoles(new IConsole[] { console });
		return console;
	}
}
