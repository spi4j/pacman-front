package fr.pacman.front.start.ui;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import fr.pacman.front.core.service.ZipUtils;

/**
 * Classe utilitaire pour créer un projet React dans Eclipse à partir d'un
 * template ZIP.
 * <p>
 * Cette classe crée le projet Eclipse, décompresse le template React/Vite et
 * rafraîchit le projet pour que les fichiers apparaissent dans l'IDE.
 */
public class ReactProjectCreator {

	/**
	 * Chemin par défaut du template ZIP React dans le plugin
	 */
	private static final String c_default_template_path = "/resources/templates/react-ts-template.zip";

	/**
	 * Crée un projet Eclipse avec le template React/Vite (sous forme de fichier zip).
	 * 
	 * Si le projet existe déjà, il sera simplement ouvert. Sinon, il sera créé. Le
	 * template ZIP est décompressé dans le répertoire du projet.
	 *
	 * @param p_projectName Nom du projet à créer
	 * @param p_monitor     Moniteur de progression Eclipse
	 * @return L'objet {@link IProject} représentant le projet créé
	 * @throws CoreException si la création ou l'ouverture du projet échoue, ou si
	 *                       la décompression du template échoue
	 */
	public static IProject createReactProject(String p_projectName, IProgressMonitor p_monitor) throws CoreException {

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(p_projectName);
		if (!project.exists()) {
			IProjectDescription desc = project.getWorkspace().newProjectDescription(p_projectName);
			project.create(desc, p_monitor);
		}
		if (!project.isOpen()) {
			project.open(p_monitor);
		}
		try (InputStream zipStream = findTemplateZip(c_default_template_path)) {
			Path targetDir = project.getLocation().toFile().toPath();
			ZipUtils.unzip(zipStream, targetDir);
		} catch (Exception e) {
			throw new CoreException((IStatus) e);
		}
		project.refreshLocal(IProject.DEPTH_INFINITE, p_monitor);
		return project;
	}

	/**
	 * Cherche le template ZIP dans le classpath ou dans le bundle du plugin.
	 * 
	 * Cette méthode tente d'abord de charger le fichier via
	 * {@code getResourceAsStream}. Si cela échoue, elle tente de le récupérer via
	 * le bundle OSGi.
	 *
	 * @param p_templatePath Chemin relatif vers le template dans le plugin
	 * @return Un {@link InputStream} pour le fichier ZIP
	 * @throws Exception si le fichier ZIP ne peut pas être trouvé
	 */
	private static InputStream findTemplateZip(String p_templatePath) throws Exception {
		InputStream zipStream = ReactProjectCreator.class.getResourceAsStream(p_templatePath);

		if (zipStream != null)
			return zipStream;

		System.out.println("ZIP introuvable via getResourceAsStream : " + p_templatePath);
		System.out.println(
				"Classpath : " + ReactProjectCreator.class.getProtectionDomain().getCodeSource().getLocation());

		// Fallback pour plugin packagé
		Bundle bundle = FrameworkUtil.getBundle(ReactProjectCreator.class);
		if (bundle != null) {
			URL url = bundle.getEntry(p_templatePath.substring(1)); // retirer le '/'
			if (url != null) {
				zipStream = url.openStream();
			}
		}

		if (zipStream == null) {
			throw new IllegalStateException(
					"Template ZIP introuvable ! Vérifiez que le fichier existe dans le classpath ou dans le plugin.");
		}
		return zipStream;
	}
}
