package fr.pacman.front.cinematic.react.main;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.acceleo.aql.AcceleoUtil;
import org.eclipse.acceleo.query.ast.TypeLiteral;
import org.eclipse.acceleo.query.runtime.namespace.IQualifiedNameQueryEnvironment;
import org.eclipse.emf.common.util.Monitor;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;

import fr.pacman.front.core.generator.PacmanGenerator;
import fr.pacman.front.core.property.project.ProjectProperties;

/**
 * Générateur pour la validation du diagramme de modélisation. Ce 'générateur'
 * scanne le fichier de modélisation et vérifie si les règles minimales sont
 * respectées.
 * 
 * Se reporter à la classe {@link PacmanGenerator} pour l'explication des
 * différentes méthodes.
 * 
 * @author MINARM
 */
public class GenValidation extends PacmanGenerator {

	@Override
	protected Map<String, SelectionType_Enum> getMainTemplates() {
		Map<String, SelectionType_Enum> templates = new HashMap<>();
		templates.put("genValidation", SelectionType_Enum.FILE);
		return templates;
	}

	@Override
	public String getSubProjectName() {
		return ProjectProperties.get_projectModelName(null);
	}

	@Override
	public String getModuleQualifiedName() {
		return "fr::pacman::front::cinematic::react::aql::genValidation";
	}

	@Override
	protected Map<String, String> getOptions() {
		Map<String, String> options = new LinkedHashMap<>();
		options.put(AcceleoUtil.LOG_URI_OPTION, c_defaultLogFileName);
		options.put(AcceleoUtil.NEW_LINE_OPTION, c_defaultNewLine);
		return options;
	}

	@Override
	protected List<EObject> getValues(IQualifiedNameQueryEnvironment p_queryEnvironment,
			Map<EClass, List<EObject>> p_valuesCache, TypeLiteral p_type, ResourceSet p_resourceSetForModels,
			Monitor p_monitor) {
		final List<EObject> values = AcceleoUtil.getValues(p_type, p_queryEnvironment,
				p_resourceSetForModels.getResources(), p_valuesCache, p_monitor);
		return values;
	}

	@Override
	public boolean doPostTreatments() {
		return false;
	}
}
