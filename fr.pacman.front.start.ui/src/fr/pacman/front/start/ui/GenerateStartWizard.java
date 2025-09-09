package fr.pacman.front.start.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import fr.pacman.front.start.ui.activator.Activator;
import fr.pacman.front.start.ui.util.WizardUtil;

/**
 * Wizard pour la création d'un projet de type Cali par le biais du
 * "new->project menu".
 * 
 * @author MINARM.
 */
public class GenerateStartWizard extends Wizard implements INewWizard {

	/**
	 * La page de configuration des différentes options de création pour le projet.
	 */
	private PropertiesWizardStartPage _pageOne;

	/**
	 * La version du générateur.
	 */
	private static final String c_version = "5.0.0";

	/**
	 * Initialise le wizard avec le workbench et la sélection courante.
	 * <p>
	 * Dans cette implémentation, la méthode configure simplement le titre de la
	 * fenêtre du wizard en incluant la version du générateur Front de Pacman.
	 *
	 * @param p_workbench le workbench Eclipse dans lequel le wizard est ouvert
	 * @param p_selection la sélection actuelle dans Eclipse (non utilisée ici)
	 */
	@Override
	public void init(IWorkbench p_workbench, IStructuredSelection p_selection) {
		setWindowTitle("Pacman : générateur de code Front [version " + c_version + "]");
	}

	/**
	 * Exécute les actions de finalisation du wizard.
	 * <p>
	 * Cette méthode est appelée lorsque l'utilisateur clique sur "Finish". Elle
	 * crée un job Eclipse pour exécuter les étapes suivantes de manière asynchrone
	 * :
	 * <ul>
	 * <li>Vérification et installation de Node.js et npm via
	 * {@link NodeInstallerHelper}.</li>
	 * <li>Initialisation des vues Eclipse nécessaires avec
	 * {@link WizardUtil#initViews(SubMonitor)}.</li>
	 * <li>Création du projet React/Vite.</li>
	 * <li>Installation des dépendances npm dans le projet via
	 * {@link #configureSubProjectsNpm(IProject, String, SubMonitor)}.</li>
	 * <li>Rafraîchissement du dossier du projet pour que Eclipse détecte les
	 * fichiers créés/modifiés.</li>
	 * </ul>
	 * Le job utilise un {@link SubMonitor} pour suivre la progression des
	 * différentes étapes. En cas d'erreur, le statut d'erreur est renvoyé via
	 * {@link WizardUtil#sendErrorStatus(Exception, String)}.
	 * </p>
	 *
	 * @return {@code true} si le wizard peut se fermer immédiatement après le
	 *         lancement du job.
	 */
	@Override
	public boolean performFinish() {
		Job job = new Job("Création du projet Frontend (React + Vite)") {
			@Override
			public IStatus run(IProgressMonitor p_monitor) {
				IProject project = null;
				SubMonitor subMonitor = SubMonitor.convert(p_monitor, 100);

				try {
					// Vérifie/installe Node + npm
					IStatus status = NodeInstallerHelper.ensureToolsReady();
					if (!status.isOK()) {
						return status;
					}
					subMonitor.setTaskName("Réinitialisation des vues");
					WizardUtil.initViews(subMonitor);
					
					subMonitor.setTaskName("Création du projet de modélisation");

					subMonitor.setTaskName("Création du projet React/Vite");
					project = createProjectReact(subMonitor);

					subMonitor.setTaskName("Installation des dépendances npm\"");
					SubMonitor npmMonitor = subMonitor.split(50); // réserve 50 unités pour npm
					configureSubProjectsNpm(project, NodeInstallerHelper.findNpmExecutable(), npmMonitor);

					if (project != null && project.exists()) {
						File projectFolder = project.getLocation().toFile();
						refreshFolderRecursively(projectFolder);
						project.refreshLocal(IResource.DEPTH_INFINITE, null);
					}

				} catch (Exception e) {
					return WizardUtil.sendErrorStatus(e, Activator.c_pluginId);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return true;
	}

	/**
	 * Met à jour récursivement la date de dernière modification d'un dossier et de
	 * tous ses fichiers/folders enfants.
	 * <p>
	 * Cette opération parcourt tous les sous-dossiers et fichiers du dossier
	 * spécifié et applique `setLastModified` avec le temps courant. Cela permet par
	 * exemple de forcer Eclipse à détecter des changements dans le système de
	 * fichiers.
	 *
	 * @param folder le dossier à rafraîchir ; doit être non null et exister sur le
	 *               disque
	 */
	private void refreshFolderRecursively(File folder) {
		if (folder.isDirectory()) {
			for (File f : folder.listFiles()) {
				refreshFolderRecursively(f);
			}
		}
		folder.setLastModified(System.currentTimeMillis());
	}

	/**
	 * Création du projet cible Java.
	 * 
	 * @param p_monitor         l'objet de monitoring pour contrôler les fichiers
	 *                          créés sous l'IDE.
	 * @param p_startProperties le tableau des propriétés pour le nouveu projet.
	 * @return le nouveau projet
	 * @throws Exception
	 */
	private IProject createProjectReact(final SubMonitor p_monitor) throws Exception {
		final String p_appliName = _pageOne.getProjectName();
		if (null == p_appliName || p_appliName.isEmpty())
			throw new NullPointerException("Le nom du projet n'est pas renseigné.");
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(p_appliName);
		if (!project.exists()) {
			ReactProjectCreator.createReactProject(project.getName(), p_monitor);
		}
		return project;
	}

	/**
	 * Configure les sous-projets d'un projet Eclipse en lançant `npm install` dans
	 * le répertoire du projet spécifié.
	 * <p>
	 * Cette méthode utilise un {@link ProcessBuilder} pour exécuter la commande
	 * npm, et lit simultanément les flux de sortie standard et d'erreur afin de
	 * mettre à jour le {@link SubMonitor} fourni avec les messages d'installation
	 * et l'avancement.
	 * <p>
	 * La méthode attend la fin du processus npm et de la lecture des flux. Si la
	 * commande retourne un code de sortie différent de 0, une exception est levée.
	 * </p>
	 *
	 * @param p_project  le projet Eclipse dont les sous-projets doivent être
	 *                   configurés avec npm
	 * @param npmExec    le chemin vers l'exécutable npm (ex. "npm" ou chemin
	 *                   absolu)
	 * @param subMonitor le {@link SubMonitor} pour suivre l'avancement et afficher
	 *                   les messages de npm
	 * @throws Exception si une erreur survient lors de l'exécution du processus ou
	 *                   de la lecture des flux
	 */
	private void configureSubProjectsNpm(final IProject p_project, String npmExec, SubMonitor subMonitor)
			throws Exception {
		File projectFolder = p_project.getLocation().toFile();
		ProcessBuilder pb = new ProcessBuilder(npmExec, "install");
		pb.directory(projectFolder);
		Process process = pb.start();

		// Threads pour stdout et stderr
		Thread stdoutThread = new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					subMonitor.subTask(line);
					subMonitor.worked(1);
				}
			} catch (Exception ignored) {
			}
		});

		Thread stderrThread = new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					subMonitor.subTask(line);
					subMonitor.worked(1);
				}
			} catch (Exception ignored) {
			}
		});

		stdoutThread.start();
		stderrThread.start();

		int exitCode = process.waitFor();
		stdoutThread.join();
		stderrThread.join();

		if (exitCode != 0) {
			throw new RuntimeException("npm install a échoué avec le code " + exitCode);
		}
	}

	/**
	 * Ajoute les pages du wizard.
	 * <p>
	 * Dans cette implémentation, seule la page de démarrage
	 * {@link PropertiesWizardStartPage} est ajoutée au wizard.
	 * <p>
	 * Cette méthode est appelée automatiquement par le framework Eclipse lors de la
	 * création du wizard.
	 */
	@Override
	public void addPages() {
		_pageOne = new PropertiesWizardStartPage();
		addPage(_pageOne);
	}
}
