package fr.pacman.front.core.property.project;

import fr.pacman.front.core.property.PacmanProperty;
import fr.pacman.front.core.property.PropertiesCategory;
import fr.pacman.front.core.property.PropertiesHandler;

public final class ProjectProperties extends PropertiesCategory {

	private static final String c_noDefaultValue = "";

	/**
	 * Liste des clés de propriétés. Obligé de les mettre en public (par
	 * simplification et éviter de mettre un pattern plus complexe) pour ne pas
	 * avoir de duplication au niveau du projet de création pour une application
	 * cible.
	 */
	static final String c_project_suffixModel = "model";
	static final String c_project_suffixServer = "server";
	public static final String c_project_debug = "project.debug.enabled";
	public static final String c_project_version = "project.version";
	public static final String c_project_name = "project.name";
	public static final String c_project_author = "project.author";
	public static final String c_project_profiler = "project.profiler.enabled";
	public static final String c_project_framework = "project.framework.type";

	@Override
	protected String get_propertiesFileName() {
		return "project.properties";
	}

	@Override
	protected boolean is_defaultFileForAdditionalproperties() {
		return true;
	}

	@Override
	protected PacmanProperty[] initPacmanProperties() {
		return new PacmanProperty[] {
				PacmanProperty.newRequired(c_project_name, c_noDefaultValue,
						"Le nom de l'application (sert de prefixe pour l'ensemble des projets)"),

				PacmanProperty.newRequired(c_project_author, System.getProperty("user.name", "MINARM"),
						"L'auteur par defaut pour les fichiers generes"),

				PacmanProperty.newRequired(c_project_profiler, "false",
						"Flag indiquant si le profiling est actif lors des generations"),

				PacmanProperty.newRequired(c_project_framework, "spring",
						"Type de framework pour le projet (React par defaut)"),

				PacmanProperty.newRequired(c_project_version, c_noDefaultValue, "La version de l'application"),

				PacmanProperty.newRequired(c_project_debug, "false",
						"Flag indiquant si la generation pour le projet fonctionne en mode debug (non par defaut)"),

		};
	}

	public static String get_projectName(final Object p_object) {
		return PropertiesHandler.getProperty(c_project_name);
	}

	public static boolean isModeDebug() {
		return Boolean.valueOf(PropertiesHandler.getProperty(c_project_debug));
	}

	public static String getVersion() {
		return PropertiesHandler.getProperty(c_project_version);
	}

	public static final String get_property(final String p_key) {
		return PropertiesHandler.getProperty(p_key);
	}

	public static String get_authorName(Object p_object) {
		return PropertiesHandler.getProperty(c_project_author);
	}
	
	public static String get_framework(final Object p_object) {
		return PropertiesHandler.getProperty(c_project_framework);
	}

	public static boolean isProfilerEnabled() {
		return Boolean.valueOf(PropertiesHandler.getProperty(c_project_profiler));
	}

	public static String get_projectServerName(final Object p_object) {
		return get_projectName(p_object) + "-" + c_project_suffixServer;
	}

	public static String get_projectModelName(final Object p_object) {
		return get_projectName(p_object) + "-" + c_project_suffixModel;
	}

	public static String get_suffixModel(final Object p_object) {
		return c_project_suffixModel;
	}

	public static String get_suffixServer(final Object p_object) {
		return c_project_suffixServer;
	}
}
