package fr.pacman.front.start.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import fr.pacman.front.core.ui.service.PlugInUtils;
import fr.pacman.front.start.ui.activator.Activator;
import fr.pacman.front.start.ui.util.WizardUtil;

/**
 * Wizard Eclipse pour la création de projets spécifiques.
 * <p>
 * Cette classe étend {@link Wizard} et implémente {@link INewWizard} afin de
 * fournir une interface guidée pour générer différents types de projets (ex :
 * projet React serveur, projet modèle, etc.) dans l’espace de travail Eclipse.
 * </p>
 * <p>
 * Le wizard inclut plusieurs pages, dont {@link PropertiesWizardStartPage} pour
 * la saisie des informations nécessaires à la création des projets.
 * </p>
 *
 * @author MINARM
 */
public class GenerateStartWizard extends Wizard implements INewWizard {

	/**
	 * Première page du wizard utilisée pour récupérer les informations nécessaires
	 * à la création des projets (nom du projet, options de configuration, etc.).
	 */
	private PropertiesWizardStartPage _pageOne;

	/**
	 * Version de l’outil ou du plugin.
	 */
	private static final String c_version = "5.0.0";

	/**
	 * Initialise le wizard avec le workbench et la sélection actuelle.
	 * <p>
	 * Cette méthode est appelée automatiquement par le framework Eclipse lors de
	 * l'ouverture du wizard.
	 * <p>
	 * Dans cette implémentation, seule la fenêtre du wizard est configurée avec un
	 * titre personnalisé incluant la version de l'outil.
	 *
	 * @param workbench l'instance du workbench Eclipse dans lequel le wizard
	 *                  s'exécute
	 * @param selection la sélection courante dans le workbench (non utilisée ici)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Pacman : générateur de code Front [version " + c_version + "]");
	}

	/**
	 * Exécute les opérations de finalisation du wizard lorsque l'utilisateur clique
	 * sur "Finish".
	 * <p>
	 * Cette méthode lance un <b>Job Eclipse</b> pour créer le projet Frontend
	 * (React + Vite) de manière asynchrone, afin de ne pas bloquer l'interface
	 * utilisateur. Le flux du Job comprend :
	 * <ul>
	 * <li>Vérification que la vue TM Terminal est installée via
	 * {@link WizardUtil#checkTerminalInstalled()}. Si elle est absente, une
	 * tentative d'installation automatique est réalisée via
	 * {@link GenrateTmTerminalInstaller#installAndOpenTerminal()} et un message
	 * d'information est affiché.</li>
	 * <li>Réinitialisation des vues Eclipse via
	 * {@link WizardUtil#initViews(SubMonitor)}.</li>
	 * <li>Création du projet de modélisation et du projet React/Vite.</li>
	 * <li>Vérification et installation de Node.js via
	 * {@link #installNodeJs(SubMonitor, IProject)}.</li>
	 * <li>Rafraîchissement complet du projet Eclipse via
	 * {@link WizardUtil#refreshProject(SubMonitor, IProject)}.</li>
	 * </ul>
	 * <p>
	 * Les exceptions sont gérées pour afficher des messages clairs et envoyer les
	 * statuts d'erreur via {@link WizardUtil#sendErrorStatus(Exception, String)}.
	 * Enfin, les vues sont restaurées dans l'état standard avec
	 * {@link WizardUtil#restaureAllView()}.
	 *
	 * @return toujours <code>true</code> car le wizard lance le Job asynchrone et
	 *         ne bloque pas l'UI.
	 */
	@Override
	public boolean performFinish() {
		Job job = new Job("Création du projet Frontend (React + Vite)") {
			@Override
			public IStatus run(IProgressMonitor p_monitor) {
				SubMonitor subMonitor = SubMonitor.convert(p_monitor, 100);
				IProject project = null;

				try {
					subMonitor.setTaskName("Vérification de l'existance de la vue tm terminale");
					WizardUtil.checkTerminalInstalled();

					subMonitor.setTaskName("Réinitialisation des vues");
					WizardUtil.initViews(subMonitor);

					subMonitor.setTaskName("Création du projet de modélisation");
					project = createProjectModel(subMonitor);

					subMonitor.setTaskName("Création du projet React/Vite");
					project = createProjectReact(subMonitor);

					subMonitor.setTaskName("Vérification et installation NodeJs");
					installNodeJs(subMonitor, project);

					subMonitor.setTaskName("Affichage des différentes vues");
					WizardUtil.restaureAllView();

					// subMonitor.setTaskName("Ouverture d'un terminal externe pour seconder tm
					// terminal");
					// WizardUtil.openTerminalExternal(project);

				} catch (IllegalStateException e) {

					PlugInUtils.displayInformation("Vue TM Terminal",
							"La vue TM Terminal n'a pas été installé dans cette version ISD."
									+ "\nVeuillez effectuer une installation manuelle "
									+ "par le biais du Marketplace.");

					// GenrateTmTerminalInstaller.installAndOpenTerminal();
					return Status.CANCEL_STATUS;

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
	 * Crée un projet Eclipse pour la couche modèle d’une application.
	 * <p>
	 * Cette méthode effectue les étapes suivantes :
	 * <ol>
	 * <li>Récupère le nom du projet à partir de la page du wizard et lui ajoute le
	 * suffixe "-model".</li>
	 * <li>Crée un projet Eclipse correspondant dans le workspace.</li>
	 * <li>Délègue la génération de la structure du projet à
	 * {@link GenerateModelProjectCreator#createProject(IProject, SubMonitor)}.</li>
	 * <li>Rafraîchit le projet pour que les changements soient visibles dans l’IDE
	 * via {@link WizardUtil#refreshProject(SubMonitor, IProject)}.</li>
	 * </ol>
	 * </p>
	 *
	 * @param p_monitor le moniteur de progression utilisé pour suivre l’avancement
	 *                  de la création du projet
	 * @return l’objet {@link IProject} représentant le projet modèle créé
	 * @throws Exception si la création du projet échoue ou si une opération sur
	 *                   l’espace de travail déclenche une erreur
	 */
	private IProject createProjectModel(final SubMonitor p_monitor) throws Exception {
		final String projectName = _pageOne.getProjectName() + "-model";
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		GenerateModelProjectCreator.createProject(project, p_monitor);
		WizardUtil.refreshProject(p_monitor, project);
		return project;
	}

	/**
	 * Crée un projet React côté serveur dans l’espace de travail Eclipse.
	 * <p>
	 * Cette méthode effectue les étapes suivantes :
	 * <ol>
	 * <li>Récupère le nom du projet à partir de la page du wizard et lui ajoute le
	 * suffixe "-server".</li>
	 * <li>Crée un projet Eclipse correspondant dans le workspace.</li>
	 * <li>Délègue la génération de la structure du projet à
	 * {@link GenerateReactProjectCreator#createProject(IProject, SubMonitor)}.</li>
	 * <li>Rafraîchit le projet pour que les changements soient visibles dans l’IDE
	 * via {@link WizardUtil#refreshProject(SubMonitor, IProject)}.</li>
	 * </ol>
	 * </p>
	 *
	 * @param p_monitor le moniteur de progression utilisé pour suivre l’avancement
	 *                  de la création du projet
	 * @return l’objet {@link IProject} représentant le projet React créé
	 * @throws Exception si la création du projet échoue ou si une opération sur
	 *                   l’espace de travail déclenche une erreur
	 */
	private IProject createProjectReact(final SubMonitor p_monitor) throws Exception {
		final String projectName = _pageOne.getProjectName() + "-server";
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		GenerateReactProjectCreator.createProject(project, p_monitor);
		WizardUtil.refreshProject(p_monitor, project);
		return project;
	}

	/**
	 * Vérifie et installe Node.js et les outils nécessaires pour le projet donné.
	 * <p>
	 * Cette méthode utilise
	 * {@link GenerateNodeInstallerHelper#ensureToolsReady(java.nio.file.Path, org.eclipse.core.runtime.IProgressMonitor)}
	 * pour s'assurer que Node.js et les outils associés sont disponibles dans le
	 * projet.
	 *
	 * @param p_monitor le sous-moniteur de progression pour suivre l'avancement de
	 *                  l'installation
	 * @param p_project le projet Eclipse pour lequel Node.js doit être installé
	 * @throws CoreException
	 * @throws RuntimeException si l'installation ou la vérification des outils
	 *                          échoue
	 */
	private void installNodeJs(final SubMonitor p_monitor, IProject p_project) throws CoreException {
		IStatus status = GenerateNodeInstallerHelper.ensureToolsReady(p_project.getLocation().toFile().toPath(),
				p_monitor.split(20));
		if (!status.isOK())
			throw new RuntimeException("");
		WizardUtil.refreshProject(p_monitor, p_project);
	}

	/**
	 * Ajoute les pages au wizard.
	 * <p>
	 * Cette implémentation crée une instance de {@link PropertiesWizardStartPage}
	 * et l'ajoute au wizard via
	 * {@link #addPage(org.eclipse.jface.wizard.IWizardPage)}.
	 * <p>
	 * Cette méthode est appelée automatiquement par le framework JFace lors de
	 * l'ouverture du wizard pour initialiser ses pages.
	 */
	@Override
	public void addPages() {
		_pageOne = new PropertiesWizardStartPage();
		addPage(_pageOne);
	}
}
