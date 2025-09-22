package fr.pacman.front.start.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import fr.pacman.front.start.ui.exception.PacmanInitModelException;
import fr.pacman.front.start.ui.util.SiriusUtil;

public class GenerateModelProjectCreator {

	public static void createProject(IProject p_project, IProgressMonitor p_monitor)
			throws CoreException, PacmanInitModelException {

		List<String> models = new ArrayList<>();
		models.add("cinematic");

		if (!p_project.exists()) {
			IProjectDescription desc = p_project.getWorkspace().newProjectDescription(p_project.getName());
			p_project.create(desc, p_monitor);
		}
		if (!p_project.isOpen()) {
			p_project.open(p_monitor);
		}

		SubMonitor monitor = SubMonitor.convert(p_monitor, "Création du projet " + p_project.getName(), 100);
		IProjectDescription description = p_project.getDescription();
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = "org.eclipse.sirius.nature.modelingproject";
		description.setNatureIds(newNatures);
		p_project.setDescription(description, null);
		SiriusUtil.addModelingResources(monitor, p_project, p_project.getName(), models);
		monitor.done();
	}
}
