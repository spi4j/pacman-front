package fr.pacman.front.config.ui.generators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.Logger;

import fr.pacman.front.config.main.GenCommon;
import fr.pacman.front.config.main.GenServer;
import fr.pacman.front.config.ui.plugin.Activator;
import fr.pacman.front.core.generator.PacmanGenerator;
import fr.pacman.front.core.ui.generator.PacmanUIGenerator;

/**
 * UI Generator for Service based on Soa model file.
 * 
 * Se reporter à la classe {@link PacmanUIGenerator} pour l'explication des
 * différentes méthodes.
 * 
 * @author MINARM
 */
public class ConfigUIGenerators extends PacmanUIGenerator {

	/**
	 * Constructeur.
	 * 
	 * @param p_selected la ressource sélectionnée (ici obligatoirement un fichier).
	 */
	public ConfigUIGenerators(final IFile p_selected) {
		super(p_selected);
	}

	@Override
	protected List<PacmanGenerator> getGenerators() {
		final List<PacmanGenerator> v_generators = new ArrayList<>();
		v_generators.add(new GenServer());
		v_generators.add(new GenCommon());
		return v_generators;
	}

	@Override
	protected String getPluginId() {
		return Activator.c_pluginId;
	}

	@Override
	protected Logger getLogger() {
		return Activator.getDefault().getPluginLogger();
	}

	@Override
	protected List<String> getIncompatibleOptions() {
		// return Arrays.asList(ProjectProperties.getIsLibraryRs());
		return null;
	}

	@Override
	protected boolean doPostTreatments() {
		return true;
	}

	@Override
	protected boolean hasView() {
		return false;
	}
}
