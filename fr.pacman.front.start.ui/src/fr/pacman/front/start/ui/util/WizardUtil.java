package fr.pacman.front.start.ui.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

import fr.pacman.front.core.ui.validation.PacmanUIValidationView;
import fr.pacman.front.start.ui.activator.Activator;
import fr.pacman.front.start.ui.views.HtmlViewerView;

/**
 * Classe utilitaire pour le wizard de création d'un nouveau projet.
 * 
 * @author MINARM
 */
public class WizardUtil {

	/**
	 * La liste des codes de retour pour l'exécution d'un wizard.
	 */
	public static final int c_codeOk = 0;
	public static final int c_codeKo = 1;
	public static final int c_codeKoExists = 2;

	private static final String c_view_console = "org.eclipse.ui.console.ConsoleView";
	private static final String c_view_log = "org.eclipse.pde.runtime.LogView";
	private static final String c_view_problem = "org.eclipse.ui.views.ProblemView";
	private static final String c_view_properties = "org.eclipse.ui.views.PropertySheet";
	private static final String c_view_junit = "org.eclipse.jdt.junit.ResultView";
	private static final String c_view_html = "fr.pacman.front.start.ui.views.HtmlViewerView";
	private static final String c_view_progress = "org.eclipse.ui.views.ProgressView";
	private static final String c_view_tm_terminal = "org.eclipse.tm.terminal.view.ui.TerminalsView";
	private static final String c_view_validation = PacmanUIValidationView.c_id;

	/**
	 * Constructeur privé.
	 */
	private WizardUtil() {
		super();
	}

	/**
	 * Vérifie si la vue Eclipse <b>TM Terminal</b> est installée et disponible.
	 * <p>
	 * La vérification se fait en recherchant la vue via son ID
	 * <code>"org.eclipse.tm.terminal.view.ui.TerminalsView"</code>. L'appel est
	 * exécuté dans le thread UI via {@link Display#syncExec(Runnable)}.
	 * </p>
	 *
	 * @throws IllegalStateException si la vue TM Terminal n'est pas installée ou
	 *                               n'a pas pu être trouvée dans l'environnement
	 *                               Eclipse.
	 */
	public static void checkTerminalInstalled() throws IllegalStateException {
		final boolean[] installed = { false };
		Display.getDefault().syncExec(() -> {
			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null) {
				IViewRegistry viewRegistry = workbench.getViewRegistry();
				IViewDescriptor viewDescriptor = viewRegistry.find(c_view_tm_terminal);
				installed[0] = viewDescriptor != null;
			}
		});
		if (!installed[0]) {
			throw new IllegalStateException("La vue TM Terminal n'est pas installée. "
					+ "\nVeuillez installer la vue TM Terminal avant de continuer.");
		}
	}

	/**
	 * Restaure le comportement standard de toutes les vues configurées dans
	 * Eclipse. Lance directement un terminal local
	 * <p>
	 * Cette méthode :
	 * <ul>
	 * <li>Exécute toutes les actions sur le <b>UI thread</b> via
	 * {@link Display#syncExec(Runnable)}.</li>
	 * <li>Stoppe le monitoring de la vue de progression via
	 * {@link ProgressViewUtils#stopMonitoring(IWorkbenchWindow)}.</li>
	 * <li>Affiche les vues principales dans un ordre défini : TM Terminal,
	 * Validation, Log, Problem, Properties, JUnit, HTML.</li>
	 * <li>Cache puis réaffiche la dernière vue pour forcer le
	 * rafraîchissement.</li>
	 * </ul>
	 * Si une vue ne peut pas être initialisée, l'exception
	 * {@link PartInitException} est capturée et ignorée pour ne pas bloquer le
	 * processus de restauration.
	 * </p>
	 *
	 * @throws CoreException si une erreur critique empêche la restauration des vues
	 */
	public static void restaureAllView(final IProject p_project) throws CoreException {
		// Restaure le comportement standard pour les vues.
		Display.getDefault().syncExec(() -> {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				ProgressViewUtils.stopMonitoring(window);
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IViewPart view = null;
					try {
						view = page.showView(c_view_tm_terminal);
						tryToOpenLocalTerminal(view, p_project);

						view = page.showView(c_view_console);
						view = page.showView(c_view_validation);
						view = page.showView(c_view_log);
						view = page.showView(c_view_problem);
						view = page.showView(c_view_properties);
						view = page.showView(c_view_junit);
						view = page.showView(c_view_html);

						page.hideView(view); // Sinon de rafraichit pas.
						view = page.showView(c_view_html);

					} catch (PartInitException e) {
						// RAS (pas grave à ce niveau, on ne vas pas polluer la création)
					} finally {
						if (view != null) {
							view.setFocus();
						}
					}
				}
			}
		});
	}

	/**
	 * Ouvre un terminal local dans Eclipse dans le contexte d'un projet donné.
	 * <p>
	 * Cette méthode utilise l'API TM Terminal pour lancer automatiquement une
	 * session de terminal (cmd.exe sur Windows, /bin/bash sur Linux/macOS) dans la
	 * racine du projet passé en paramètre. L'exécution est asynchrone sur le thread
	 * UI d'Eclipse.
	 * </p>
	 *
	 * <p>
	 * Note : cette méthode doit être appelée sur le thread UI via
	 * {@link Display#asyncExec(Runnable)}.
	 * </p>
	 *
	 * @param p_view    La vue Eclipse à partir de laquelle ouvrir le terminal. Si
	 *                  nul, la méthode retourne sans action.
	 * @param p_project Le projet Eclipse dont la racine sera utilisée comme
	 *                  répertoire de travail du terminal.
	 */
	public static void tryToOpenLocalTerminal(final IViewPart p_view, IProject p_project) {
		Display.getDefault().asyncExec(() -> {
			if (p_view == null)
				return;

			ILauncherDelegate delegate = LauncherDelegateManager.getInstance()
					.getLauncherDelegate("org.eclipse.tm.terminal.connector.local.launcher.local", true);

			if (delegate == null) {
				System.err.println("Launcher local introuvable !");
				return;
			}

			Map<String, Object> props = new HashMap<>();
			props.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID,
					"org.eclipse.tm.terminal.connector.local.launcher.local");
			props.put(ITerminalsConnectorConstants.PROP_TITLE, "Terminal Local");
			props.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, Boolean.TRUE);

			if (System.getProperty("os.name").toLowerCase().contains("win")) {
				props.put(ITerminalsConnectorConstants.PROP_PROCESS_PATH, "cmd.exe");
				props.put(ITerminalsConnectorConstants.PROP_PROCESS_ARGS, "/k");
			} else {
				props.put(ITerminalsConnectorConstants.PROP_PROCESS_PATH, "/bin/bash");
				props.put(ITerminalsConnectorConstants.PROP_PROCESS_ARGS, "-l");
			}
			String workingDir = p_project.getLocation().toOSString();
			props.put(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR, workingDir);
			delegate.execute(props, null);
		});
	}

	/**
	 * Initialise les vues dans la fenêtre de travail active, en masquant toutes les
	 * vues ouvertes et en affichant la vue de progression des tâches. Cette méthode
	 * est exécutée de manière asynchrone sur le thread de l'UI.
	 * 
	 * Elle supprime toutes les vues actuellement ouvertes dans la page de travail
	 * active, puis affiche la vue de progression des tâches et commence à
	 * surveiller les tâches en cours avec
	 * {@link ProgressViewUtils#monitor(IWorkbenchWindow)}.
	 *
	 * Cette méthode est principalement utilisée avant de lancer un job ou une série
	 * de tâches asynchrones, afin de réinitialiser les vues et d'afficher un
	 * indicateur de progression. En cas d'erreur lors de l'affichage de la vue, une
	 * exception est lancée.
	 * 
	 * @param p_monitor Un objet {@link SubMonitor} utilisé pour suivre la
	 *                  progression de l'opération. Il est utilisé pour la gestion
	 *                  de la progression dans les tâches asynchrones (bien que non
	 *                  directement utilisé dans cette méthode, il pourrait être
	 *                  intégré pour la gestion des sous-tâches).
	 * 
	 * @throws RuntimeException Si une erreur se produit lors de l'affichage de la
	 *                          vue de progression, notamment en cas
	 *                          d'initialisation échouée de la vue ou d'une
	 *                          exception dans la méthode
	 *                          {@link IWorkbenchPage#showView(String)}.
	 */
	public static void initViews(final SubMonitor p_monitor) {
		Display.getDefault().syncExec(() -> {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					try {
						page.showView(c_view_progress);
						ProgressViewUtils.monitor(window);
					} catch (PartInitException e) {
						throw new RuntimeException("Impossible d'afficher la vue pour la progression des tâches", e);
					}
				}
			}
		});
	}

	/**
	 * Rafraîchit complètement le projet Eclipse donné en synchronisant le système
	 * de fichiers et les ressources Eclipse.
	 * <p>
	 * Cette méthode effectue les opérations suivantes :
	 * <ul>
	 * <li>Vérifie que le projet n'est pas null et qu'il existe.</li>
	 * <li>Parcourt récursivement tous les fichiers et dossiers du projet pour
	 * mettre à jour leur date de dernière modification via
	 * {@link File#setLastModified(long)}.</li>
	 * <li>Appelle
	 * {@link IProject#refreshLocal(int, org.eclipse.core.runtime.IProgressMonitor)}
	 * avec {@link IResource#DEPTH_INFINITE} pour forcer Eclipse à resynchroniser
	 * toutes les ressources du projet.</li>
	 * </ul>
	 *
	 * @param p_monitor le sous-moniteur de progression (utilisé pour suivre
	 *                  l’avancement)
	 * @param p_project le projet Eclipse à rafraîchir
	 * @throws CoreException si une erreur survient lors de l’actualisation des
	 *                       ressources Eclipse
	 */
	public static void refreshProject(final SubMonitor p_monitor, final IProject p_project) throws CoreException {
		if (null != p_project && p_project.exists()) {
			File folder = p_project.getLocation().toFile();
			refreshFolderRecursively(folder);
			p_project.refreshLocal(IResource.DEPTH_INFINITE, null);
		}
	}

	/**
	 * Parcourt récursivement un dossier et ses sous-dossiers pour mettre à jour la
	 * date de dernière modification de chaque fichier et dossier.
	 * <p>
	 * Cette opération est utilisée pour forcer Eclipse à détecter les changements
	 * lors du rafraîchissement du projet.
	 *
	 * @param folder le dossier à parcourir et rafraîchir
	 */
	private static void refreshFolderRecursively(File folder) {
		if (folder.isDirectory()) {
			for (File f : folder.listFiles()) {
				refreshFolderRecursively(f);
			}
		}
		folder.setLastModified(System.currentTimeMillis());
	}

	/**
	 * Retourne un IStatus directement dans une CoreException.
	 * 
	 * @param p_e l'exception
	 * @return status le statut
	 */
	public static IStatus sendErrorStatus(Exception p_e, final String p_pluginId) {
		IStatus status = new Status(IStatus.ERROR, p_pluginId, p_e.getMessage(), p_e);
		Activator.getDefault().getLog().log(status);
		return status;
	}

	/**
	 * Affiche un message dans une boite de dialogue.
	 * 
	 * @param p_title   le titre de la boite de dialogue.
	 * @param p_message le message à afficher dans la boite de dialogue.
	 */
	public static void displayMessageInDialog(final String p_title, final String p_message) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), p_title,
						p_message);
			}
		});
	}

	/**
	 * Affiche une page HTML à partir d'un chemin de fichier. Si un navigateur
	 * interne est disponible, il est utilisé, sinon un navigateur externe est
	 * ouvert.
	 *
	 * @param p_path Le chemin absolu du fichier HTML à afficher.
	 */
	public static void showHtml(String p_path) {
		Display.getDefault().syncExec(() -> {
			try {
				File file = new File(p_path);
				if (file.exists()) {
					URL fileUrl = file.toURI().toURL();
					boolean browserAvailable = tryOpenInternalBrowser(fileUrl.toString());
					if (!browserAvailable) {
						openExternalBrowser(fileUrl);
					}
				} else {
					// RAS (pas grave dans notre cas)
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Essaie d'ouvrir une page HTML dans un navigateur interne (dans une vue
	 * Eclipse).
	 *
	 * @param p_url L'URL de la page HTML à afficher.
	 * @return true si le navigateur interne a été ouvert avec succès, false sinon.
	 */
	private static boolean tryOpenInternalBrowser(String p_url) {
		try {
			HtmlViewerView.setHtmlUrlToLoad(p_url);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			page.showView(HtmlViewerView.c_id);
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	/**
	 * Ouvre un fichier HTML dans un navigateur externe (par exemple, le navigateur
	 * par défaut).
	 *
	 * @param p_url L'URL de la page HTML à afficher.
	 */
	private static void openExternalBrowser(URL p_url) {
		try {
			IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
			IWebBrowser browser = browserSupport.getExternalBrowser();
			browser.openURL(p_url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Convertit un fichier Markdown en HTML et l'affiche dans un navigateur interne
	 * ou externe.
	 *
	 * @param p_markdownPath Le chemin du fichier Markdown à convertir et afficher.
	 */
	public static void showHtmlFromMarkdown(String p_markdownPath) {
		try {

			String markdown = new String(Files.readAllBytes(Paths.get(p_markdownPath)), "UTF-8");
			Parser parser = Parser.builder().build();
			HtmlRenderer renderer = HtmlRenderer.builder().build();
			String htmlContent = renderer.render(parser.parse(markdown));

			String htmlWithCharset = "<!DOCTYPE html>\n<html lang=\"fr\">\n<head>\n<meta charset=\"UTF-8\">"
					+ "\n<title>Markdown Converti</title>\n</head>\n<body>\n" + htmlContent + "\n</body>\n</html>";

			File tempHtmlFile = File.createTempFile("converted", ".html");
			tempHtmlFile.deleteOnExit();
			Files.write(tempHtmlFile.toPath(), htmlWithCharset.getBytes("UTF-8"));
			showHtml(tempHtmlFile.getAbsolutePath());

		} catch (IOException e) {
			// RAS (pas grave dans notre cas)
		}
	}
}
