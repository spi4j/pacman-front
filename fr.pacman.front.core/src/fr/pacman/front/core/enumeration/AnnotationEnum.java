package fr.pacman.front.core.enumeration;

/**
 * Enumération contenant l'ensemble des méta-données possibles dans les
 * différents modèles.
 * 
 * @author MINARM
 */
public enum AnnotationEnum {
	
	INPUT_PLACEHOLDER("TXT_PLACEHOLDER", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_INFO("TXT_INFO", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_ERROR("TXT_ERROR", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_SUCCESS("TXT_VALID", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_REGEX("WITH_PATTERN", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_ICON("WITH_ICON", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_MAXLENGTH("WITH_MAXLENGTH", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_BUTTON_ADDON("WITH_BUTTON_ADDON", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_BUTTON_ACTION("WITH_BUTTON_ACTION", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_TEXTAREA("WITH_TEXTAREA", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_HINT("WITH_HINT", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_VALUE("WITH_VALUE", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_VALUE_MAX("WITH_VALUE_MAX", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_VALUE_MIN("WITH_VALUE_MIN", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_HIDE_MIN_MAX("WITH_HIDE_MIN_MAX", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_VALUES("WITH_VALUES", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_DOUBLE("WITH_DOUBLE", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_STEPS("WITH_STEPS", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_PREFIX("WITH_PREFIX", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_SUFFIX("WITH_SUFFIX", ModelEnum.DSL_CINEMATIC, true),
	
	INPUT_WITH_REQUIRED("WITH_REQUIRED", ModelEnum.DSL_CINEMATIC, true),

	INPUT_WITH_MSG_GROUP("WITH_MSG_GROUP", ModelEnum.DSL_CINEMATIC, true),

	PASSWORD_DESCRIPTION("TXT_DESCRIPTION", ModelEnum.DSL_CINEMATIC, true);

	/** Le nom de l'annotation */
	private String _annotationName;

	/** Le type de modèle DSM sur laquelle on l'a trouve */
	// private ModelEnum[] _tab_AnotationModel;
	private ModelEnum _tab_annotationModel;

	/** le boolean qui indique si la métadonnées a un contenu */
	private boolean _content;

	/**
	 * Constructeur avec le nom et les DSM de l'annotation.
	 * 
	 * @param p_annotationName      Le nom de l'annotation.
	 * @param p_tab_annotationModel Le (ou les) DSM sur lesquelles elle s'applique.
	 * @param p_content             'true' si l'annotation possède un contenu.
	 */
	private AnnotationEnum(final String p_annotationName, final ModelEnum p_tab_annotationModel,
			final boolean p_content) {
		_annotationName = p_annotationName;
		_tab_annotationModel = p_tab_annotationModel;
		_content = p_content;
	}

	/**
	 * Retourne le nom de l'annotation.
	 * 
	 * @return Le nom de l'annotation.
	 */
	public String get_annotationName() {
		return _annotationName;
	}

	/**
	 * Boolean qui indique si la métadonnée a un contenu.
	 * 
	 * @return boolean : 'true' si l'annotation doit avoir un cootenu, sinon,
	 *         retourne la valeur 'false'.
	 */
	public boolean hasBody() {
		return _content;
	}

	/**
	 * Retourne Le (ou les) DSM sur lesquels l'annotation s'applique.
	 * 
	 * @return Le tableau contenant les DSM potentiels.
	 */
	public ModelEnum get_tabAnnotationModel() {
		return _tab_annotationModel;
	}

	/**
	 * Retourne une annotation à partir de son titre
	 * 
	 * @param p_titre Le titre de l'annotation
	 * @return Le tableau contenant les types de DSM potentiels.
	 */
	public static AnnotationEnum findByName(final String p_titre) {
		AnnotationEnum a = null;
		for (AnnotationEnum ae : AnnotationEnum.values()) {
			if (ae.get_annotationName().equalsIgnoreCase(p_titre)) {
				a = ae;
			}
		}
		return a;
	}
}
