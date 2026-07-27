package lsat.i18n;

import java.util.*;

/**
 * LanguagePack — Provides localized question text and UI strings for all
 * supported languages:
 *   - English (default)
 *   - Spanish (Español)
 *   - French (Français)
 *   - American Prudent (formal American English with legal/prudential framing)
 *   - German (Deutsch)
 *   - Greek (Ελληνικά)
 *
 * Each language pack contains:
 *   - UI labels (buttons, titles, prompts)
 *   - Translated question sets (Easy through Expert)
 */
public class LanguagePack {

    private final String languageCode;
    private final Map<String, String> uiStrings;
    private final String[] easyQuestions;
    private final String[] moderateQuestions;
    private final String[] mediumQuestions;
    private final String[] hardQuestions;
    private final String[] expertQuestions;

    private LanguagePack(String code, Map<String, String> ui,
                         String[] easy, String[] moderate, String[] medium,
                         String[] hard, String[] expert) {
        this.languageCode = code;
        this.uiStrings = ui;
        this.easyQuestions = easy;
        this.moderateQuestions = moderate;
        this.mediumQuestions = medium;
        this.hardQuestions = hard;
        this.expertQuestions = expert;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Accessors
    // ─────────────────────────────────────────────────────────────────────────

    public String getLanguageCode() { return languageCode; }
    public String getUI(String key) { return uiStrings.getOrDefault(key, key); }
    public String[] getEasyQuestions() { return easyQuestions; }
    public String[] getModerateQuestions() { return moderateQuestions; }
    public String[] getMediumQuestions() { return mediumQuestions; }
    public String[] getHardQuestions() { return hardQuestions; }
    public String[] getExpertQuestions() { return expertQuestions; }

    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    public static LanguagePack load(String language) {
        switch (language.toLowerCase()) {
            case "spanish": return createSpanish();
            case "french": return createFrench();
            case "american_prudent": return createAmericanPrudent();
            case "german": return createGerman();
            case "greek": return createGreek();
            default: return createEnglish();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // English (Default)
    // ─────────────────────────────────────────────────────────────────────────

    private static LanguagePack createEnglish() {
        Map<String, String> ui = new HashMap<>();
        ui.put("title", "LSAT Moral Assessment");
        ui.put("subtitle", "Adaptive Ethical Character Evaluation");
        ui.put("btn_start", "Begin Adaptive Test");
        ui.put("btn_admin", "Admin: Adjust Curve & Pacing");
        ui.put("btn_yes", "YES (Y)");
        ui.put("btn_no", "NO (N)");
        ui.put("btn_break", "☕ Break (2 min)");
        ui.put("btn_retake", "Retake Test");
        ui.put("btn_back", "Back to Welcome");
        ui.put("results_title", "Assessment Complete");
        ui.put("intellect", "Intellect");
        ui.put("morale", "Morale");
        ui.put("streak", "Streak");
        ui.put("difficulty", "Difficulty");
        ui.put("question", "Question");
        ui.put("of", "of");
        ui.put("pass", "PASS");
        ui.put("at_risk", "AT RISK");
        ui.put("copyright", "© 2026 Max Rupplin — All Rights Reserved");

        return new LanguagePack("english", ui,
                ENGLISH_EASY, ENGLISH_MODERATE, ENGLISH_MEDIUM,
                ENGLISH_HARD, ENGLISH_EXPERT);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Spanish (Español)
    // ─────────────────────────────────────────────────────────────────────────

    private static LanguagePack createSpanish() {
        Map<String, String> ui = new HashMap<>();
        ui.put("title", "Evaluación Moral LSAT");
        ui.put("subtitle", "Evaluación Adaptativa del Carácter Ético");
        ui.put("btn_start", "Comenzar Prueba Adaptativa");
        ui.put("btn_admin", "Admin: Ajustar Curva y Ritmo");
        ui.put("btn_yes", "SÍ (Y)");
        ui.put("btn_no", "NO (N)");
        ui.put("btn_break", "☕ Descanso (2 min)");
        ui.put("btn_retake", "Repetir Prueba");
        ui.put("btn_back", "Volver al Inicio");
        ui.put("results_title", "Evaluación Completa");
        ui.put("intellect", "Intelecto");
        ui.put("morale", "Moral");
        ui.put("streak", "Racha");
        ui.put("difficulty", "Dificultad");
        ui.put("question", "Pregunta");
        ui.put("of", "de");
        ui.put("pass", "APROBADO");
        ui.put("at_risk", "EN RIESGO");
        ui.put("copyright", "© 2026 Max Rupplin — Todos los Derechos Reservados");

        return new LanguagePack("spanish", ui,
                SPANISH_EASY, SPANISH_MODERATE, SPANISH_MEDIUM,
                SPANISH_HARD, SPANISH_EXPERT);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // French (Français)
    // ─────────────────────────────────────────────────────────────────────────

    private static LanguagePack createFrench() {
        Map<String, String> ui = new HashMap<>();
        ui.put("title", "Évaluation Morale LSAT");
        ui.put("subtitle", "Évaluation Adaptative du Caractère Éthique");
        ui.put("btn_start", "Commencer le Test Adaptatif");
        ui.put("btn_admin", "Admin: Ajuster la Courbe et le Rythme");
        ui.put("btn_yes", "OUI (Y)");
        ui.put("btn_no", "NON (N)");
        ui.put("btn_break", "☕ Pause (2 min)");
        ui.put("btn_retake", "Reprendre le Test");
        ui.put("btn_back", "Retour à l'Accueil");
        ui.put("results_title", "Évaluation Terminée");
        ui.put("intellect", "Intellect");
        ui.put("morale", "Morale");
        ui.put("streak", "Série");
        ui.put("difficulty", "Difficulté");
        ui.put("question", "Question");
        ui.put("of", "de");
        ui.put("pass", "RÉUSSI");
        ui.put("at_risk", "À RISQUE");
        ui.put("copyright", "© 2026 Max Rupplin — Tous Droits Réservés");

        return new LanguagePack("french", ui,
                FRENCH_EASY, FRENCH_MODERATE, FRENCH_MEDIUM,
                FRENCH_HARD, FRENCH_EXPERT);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // American Prudent (formal legal/prudential American English)
    // ─────────────────────────────────────────────────────────────────────────

    private static LanguagePack createAmericanPrudent() {
        Map<String, String> ui = new HashMap<>();
        ui.put("title", "LSAT Moral Character Assessment");
        ui.put("subtitle", "Adaptive Evaluation of Ethical Prudence & Judgment");
        ui.put("btn_start", "Commence Adaptive Examination");
        ui.put("btn_admin", "Administrator: Adjust Assessment Parameters");
        ui.put("btn_yes", "AFFIRM (Y)");
        ui.put("btn_no", "DECLINE (N)");
        ui.put("btn_break", "☕ Recess (2 min)");
        ui.put("btn_retake", "Re-Examine");
        ui.put("btn_back", "Return to Landing");
        ui.put("results_title", "Examination Concluded");
        ui.put("intellect", "Intellectual Capacity");
        ui.put("morale", "Moral Standing");
        ui.put("streak", "Consecutive Correct");
        ui.put("difficulty", "Complexity Level");
        ui.put("question", "Item");
        ui.put("of", "of");
        ui.put("pass", "SATISFACTORY");
        ui.put("at_risk", "DEFICIENT");
        ui.put("copyright", "© 2026 Max Rupplin — All Rights Reserved Under Law");

        return new LanguagePack("american_prudent", ui,
                AMERICAN_PRUDENT_EASY, AMERICAN_PRUDENT_MODERATE, AMERICAN_PRUDENT_MEDIUM,
                AMERICAN_PRUDENT_HARD, AMERICAN_PRUDENT_EXPERT);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // German (Deutsch)
    // ─────────────────────────────────────────────────────────────────────────

    private static LanguagePack createGerman() {
        Map<String, String> ui = new HashMap<>();
        ui.put("title", "LSAT Moralische Bewertung");
        ui.put("subtitle", "Adaptive Ethische Charakterbewertung");
        ui.put("btn_start", "Adaptiven Test Beginnen");
        ui.put("btn_admin", "Admin: Kurve und Tempo Anpassen");
        ui.put("btn_yes", "JA (Y)");
        ui.put("btn_no", "NEIN (N)");
        ui.put("btn_break", "☕ Pause (2 Min)");
        ui.put("btn_retake", "Test Wiederholen");
        ui.put("btn_back", "Zurück zum Start");
        ui.put("results_title", "Bewertung Abgeschlossen");
        ui.put("intellect", "Intellekt");
        ui.put("morale", "Moral");
        ui.put("streak", "Serie");
        ui.put("difficulty", "Schwierigkeit");
        ui.put("question", "Frage");
        ui.put("of", "von");
        ui.put("pass", "BESTANDEN");
        ui.put("at_risk", "GEFÄHRDET");
        ui.put("copyright", "© 2026 Max Rupplin — Alle Rechte Vorbehalten");

        return new LanguagePack("german", ui,
                GERMAN_EASY, GERMAN_MODERATE, GERMAN_MEDIUM,
                GERMAN_HARD, GERMAN_EXPERT);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Greek (Ελληνικά)
    // ─────────────────────────────────────────────────────────────────────────

    private static LanguagePack createGreek() {
        Map<String, String> ui = new HashMap<>();
        ui.put("title", "Ηθική Αξιολόγηση LSAT");
        ui.put("subtitle", "Προσαρμοστική Αξιολόγηση Ηθικού Χαρακτήρα");
        ui.put("btn_start", "Έναρξη Προσαρμοστικής Δοκιμασίας");
        ui.put("btn_admin", "Διαχείριση: Ρύθμιση Καμπύλης");
        ui.put("btn_yes", "ΝΑΙ (Y)");
        ui.put("btn_no", "ΟΧΙ (N)");
        ui.put("btn_break", "☕ Διάλειμμα (2 λεπτά)");
        ui.put("btn_retake", "Επανάληψη");
        ui.put("btn_back", "Επιστροφή");
        ui.put("results_title", "Αξιολόγηση Ολοκληρώθηκε");
        ui.put("intellect", "Νοημοσύνη");
        ui.put("morale", "Ηθική");
        ui.put("streak", "Σερί");
        ui.put("difficulty", "Δυσκολία");
        ui.put("question", "Ερώτηση");
        ui.put("of", "από");
        ui.put("pass", "ΕΠΙΤΥΧΙΑ");
        ui.put("at_risk", "ΣΕ ΚΙΝΔΥΝΟ");
        ui.put("copyright", "© 2026 Max Rupplin — Με Επιφύλαξη Παντός Δικαιώματος");

        return new LanguagePack("greek", ui,
                GREEK_EASY, GREEK_MODERATE, GREEK_MEDIUM,
                GREEK_HARD, GREEK_EXPERT);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // QUESTION DATA — All languages, all difficulty tiers
    // ═══════════════════════════════════════════════════════════════════════════

    // ── ENGLISH ──────────────────────────────────────────────────────────────

    private static final String[] ENGLISH_EASY = {
        "Do you correct a cashier who gives you too much change back?",
        "Do you hold the elevator or door for someone walking far behind you?",
        "Do you clean up after yourself in public spaces and parks?",
        "Do you arrive on time for appointments out of respect for others?",
        "Do you keep your music down in residential areas late at night?",
        "Do you give people physical space in public lines and crowds?",
        "Do you let another driver merge into your lane during heavy traffic?",
        "Do you pause your day to help someone pick up dropped items?",
        "Do you offer your seat on public transit to someone who needs it?",
        "Do you pick up litter on public sidewalks that is not yours?",
        "Do you knock and wait for permission before entering private rooms?",
        "Do you ask before borrowing items from family or roommates?",
        "Do you return lost items or money to the rightful owner immediately?",
        "Do you show up to events you promised to attend, rain or shine?",
        "Do you express gratitude frequently to those who provide you service?"
    };

    private static final String[] ENGLISH_MODERATE = {
        "Do you refuse to lie even if a small white lie makes a conversation smoother?",
        "Do you admit your mistakes immediately to your boss or peers?",
        "Do you tell a friend the truth when they ask for honest, difficult feedback?",
        "Do you refuse to spread rumors or unverified gossip?",
        "Do you admit when you do not know the answer to a question?",
        "Do you avoid exaggerating your accomplishments on your resume?",
        "Do you listen to others without interrupting or judging their experiences?",
        "Do you show patience to customer service workers who make mistakes?",
        "Do you forgive people who genuinely apologize for hurting you?",
        "Do you refrain from mocking people for their flaws or insecurities?",
        "Do you treat service workers with the same respect as executives?",
        "Do you manage your personal debts and pay them back promptly?",
        "Do you accept constructive feedback without becoming defensive?",
        "Do you follow through on group projects so you do not let partners down?",
        "Do you value other people's time as much as you value your own?"
    };

    private static final String[] ENGLISH_MEDIUM = {
        "Do you report your exact income on your taxes without cutting corners?",
        "Do you refuse to take credit for work done by a colleague?",
        "Do you keep promises even when a better opportunity arises later?",
        "Do you speak up when someone misrepresents facts in a meeting?",
        "Do you honor verbal agreements even if no contract is signed?",
        "Do you reveal product flaws if you are selling a used item?",
        "Do you defend an absent person when false statements are made about them?",
        "Do you actively try to understand the perspective of people you dislike?",
        "Do you judge people solely on their character rather than their background?",
        "Do you call out systemic bias or discrimination when you witness it?",
        "Do you vote for policies that benefit society even if they raise your taxes?",
        "Do you refuse to use nepotism to get ahead in your career?",
        "Do you support criminal justice reforms aimed at rehabilitating people?",
        "Do you uphold professional ethics even if it risks your employment?",
        "Do you protect the privacy and confidential secrets of your friends?",
        "Do you keep secrets that your friends trusted you to hold?",
        "Do you refuse bribes intended to make you betray your team?",
        "Do you remain loyal to your core values when tempted by fast money?"
    };

    private static final String[] ENGLISH_HARD = {
        "Do you state your true intentions early in a new relationship?",
        "Do you refuse to ghost people to avoid an awkward conversation?",
        "Do you sacrifice your personal free time to help a friend move?",
        "Do you regularly donate money or resources to causes helping the poor?",
        "Do you give food or money directly to homeless individuals?",
        "Do you advocate for equal pay for equal work in your workplace?",
        "Do you call out double standards when applied to different genders?",
        "Do you reject privileges gained at the direct expense of others?",
        "Do you challenge unfair policies implemented by your own government?",
        "Do you step down from leadership if you are no longer fit to serve?",
        "Do you support a person's right to bodily autonomy and medical choice?",
        "Do you support your long-term friends when they experience poverty?",
        "Do you support your partner through long periods of chronic illness?",
        "Do you stand by a friend even when they are socially unpopular?",
        "Do you honor non-disclosure agreements even after leaving a company?"
    };

    private static final String[] ENGLISH_EXPERT = {
        "Do you give anonymous donations where you receive absolutely no credit?",
        "Do you pull over to help a stranded motorist on a dark highway?",
        "Do you step in to break up a fight or defend a victim of bullying?",
        "Do you give up your weekend to assist with disaster relief efforts?",
        "Do you pass up a promotion if a colleague needs it far more urgently?",
        "Do you dive into danger to pull someone out of harm's way?",
        "Do you host or shelter someone who has nowhere else to go safely?",
        "Do you put the safety of others ahead of your own during a crisis?",
        "Do you fulfill promises made to people who have passed away?",
        "Do you protect vulnerable whistleblowers who trust you with info?",
        "Do you return to help communities that raised or supported you?",
        "Do you keep your word to your enemies or rivals without fail?",
        "Do you recuse yourself from decisions where you have a conflict of interest?"
    };

    // ── SPANISH ──────────────────────────────────────────────────────────────

    private static final String[] SPANISH_EASY = {
        "¿Corriges a un cajero que te da demasiado cambio?",
        "¿Sostienes el ascensor o la puerta para alguien que camina lejos detrás?",
        "¿Limpias después de ti mismo en espacios públicos y parques?",
        "¿Llegas a tiempo a las citas por respeto a los demás?",
        "¿Bajas tu música en áreas residenciales tarde en la noche?",
        "¿Das espacio físico a las personas en filas y multitudes públicas?",
        "¿Dejas que otro conductor se incorpore durante tráfico pesado?",
        "¿Pausas tu día para ayudar a alguien a recoger objetos caídos?",
        "¿Ofreces tu asiento en transporte público a quien lo necesita?",
        "¿Recoges basura en aceras públicas que no es tuya?",
        "¿Tocas y esperas permiso antes de entrar en habitaciones privadas?",
        "¿Preguntas antes de tomar prestados objetos de familia o compañeros?",
        "¿Devuelves objetos perdidos o dinero al dueño legítimo inmediatamente?",
        "¿Te presentas a eventos que prometiste asistir, llueva o truene?",
        "¿Expresas gratitud frecuentemente a quienes te brindan servicio?"
    };

    private static final String[] SPANISH_MODERATE = {
        "¿Te niegas a mentir incluso si una pequeña mentira facilita la conversación?",
        "¿Admites tus errores inmediatamente ante tu jefe o compañeros?",
        "¿Le dices la verdad a un amigo cuando pide retroalimentación honesta y difícil?",
        "¿Te niegas a difundir rumores o chismes no verificados?",
        "¿Admites cuando no sabes la respuesta a una pregunta?",
        "¿Evitas exagerar tus logros en tu currículum?",
        "¿Escuchas a otros sin interrumpir ni juzgar sus experiencias?",
        "¿Muestras paciencia con trabajadores de servicio que cometen errores?",
        "¿Perdonas a personas que se disculpan genuinamente por herirte?",
        "¿Te abstienes de burlarte de las personas por sus defectos?",
        "¿Tratas a trabajadores de servicio con el mismo respeto que a ejecutivos?",
        "¿Manejas tus deudas personales y las pagas puntualmente?",
        "¿Aceptas críticas constructivas sin ponerte a la defensiva?",
        "¿Cumples en proyectos grupales para no dejar a tus compañeros?",
        "¿Valoras el tiempo de otras personas tanto como el tuyo?"
    };

    private static final String[] SPANISH_MEDIUM = {
        "¿Reportas tu ingreso exacto en tus impuestos sin atajos?",
        "¿Te niegas a tomar crédito por trabajo hecho por un colega?",
        "¿Cumples promesas incluso cuando surge una mejor oportunidad después?",
        "¿Hablas cuando alguien tergiversa hechos en una reunión?",
        "¿Honras acuerdos verbales incluso si no hay contrato firmado?",
        "¿Revelas defectos del producto si estás vendiendo un artículo usado?",
        "¿Defiendes a una persona ausente cuando hacen declaraciones falsas sobre ella?",
        "¿Intentas activamente entender la perspectiva de personas que te desagradan?",
        "¿Juzgas a las personas solo por su carácter y no por su origen?",
        "¿Denuncias el sesgo sistémico o la discriminación cuando lo presencias?",
        "¿Votas por políticas que benefician a la sociedad aunque aumenten tus impuestos?",
        "¿Te niegas a usar el nepotismo para avanzar en tu carrera?",
        "¿Apoyas reformas de justicia criminal orientadas a rehabilitar personas?",
        "¿Mantienes la ética profesional aunque arriesgue tu empleo?",
        "¿Proteges la privacidad y secretos confidenciales de tus amigos?",
        "¿Guardas secretos que tus amigos te confiaron?",
        "¿Rechazas sobornos destinados a hacerte traicionar a tu equipo?",
        "¿Permaneces leal a tus valores fundamentales cuando te tienta el dinero fácil?"
    };

    private static final String[] SPANISH_HARD = {
        "¿Declaras tus verdaderas intenciones temprano en una nueva relación?",
        "¿Te niegas a desaparecer para evitar una conversación incómoda?",
        "¿Sacrificas tu tiempo libre personal para ayudar a un amigo a mudarse?",
        "¿Donas dinero o recursos regularmente a causas que ayudan a los pobres?",
        "¿Das comida o dinero directamente a personas sin hogar?",
        "¿Abogan por igual pago por igual trabajo en tu lugar de trabajo?",
        "¿Denuncias dobles estándares aplicados a diferentes géneros?",
        "¿Rechazas privilegios obtenidos a costa directa de otros?",
        "¿Desafías políticas injustas implementadas por tu propio gobierno?",
        "¿Renuncias al liderazgo si ya no eres apto para servir?",
        "¿Apoyas el derecho de una persona a la autonomía corporal y elección médica?",
        "¿Apoyas a tus amigos de largo plazo cuando experimentan pobreza?",
        "¿Apoyas a tu pareja durante largos períodos de enfermedad crónica?",
        "¿Permaneces junto a un amigo incluso cuando es socialmente impopular?",
        "¿Honras acuerdos de confidencialidad incluso después de dejar una empresa?"
    };

    private static final String[] SPANISH_EXPERT = {
        "¿Haces donaciones anónimas donde no recibes absolutamente ningún crédito?",
        "¿Te detienes para ayudar a un motorista varado en una carretera oscura?",
        "¿Intervienes para detener una pelea o defender a una víctima de acoso?",
        "¿Renuncias a tu fin de semana para asistir en esfuerzos de socorro por desastres?",
        "¿Rechazas una promoción si un colega la necesita mucho más urgentemente?",
        "¿Te sumerges en el peligro para sacar a alguien del daño?",
        "¿Albergas o refugias a alguien que no tiene dónde más ir de forma segura?",
        "¿Pones la seguridad de otros por delante de la tuya durante una crisis?",
        "¿Cumples promesas hechas a personas que han fallecido?",
        "¿Proteges a denunciantes vulnerables que te confían información?",
        "¿Regresas para ayudar a comunidades que te criaron o apoyaron?",
        "¿Mantienes tu palabra con tus enemigos o rivales sin falta?",
        "¿Te recusas de decisiones donde tienes un conflicto de intereses?"
    };

    // ── FRENCH ───────────────────────────────────────────────────────────────

    private static final String[] FRENCH_EASY = {
        "Corrigez-vous un caissier qui vous rend trop de monnaie?",
        "Tenez-vous l'ascenseur ou la porte pour quelqu'un qui marche loin derrière?",
        "Nettoyez-vous après vous dans les espaces publics et les parcs?",
        "Arrivez-vous à l'heure aux rendez-vous par respect pour les autres?",
        "Baissez-vous votre musique dans les zones résidentielles tard le soir?",
        "Donnez-vous de l'espace physique aux gens dans les files et les foules?",
        "Laissez-vous un autre conducteur s'insérer pendant les embouteillages?",
        "Faites-vous une pause pour aider quelqu'un à ramasser des objets tombés?",
        "Offrez-vous votre siège dans les transports en commun à qui en a besoin?",
        "Ramassez-vous les déchets sur les trottoirs publics qui ne sont pas les vôtres?",
        "Frappez-vous et attendez-vous la permission avant d'entrer dans des pièces privées?",
        "Demandez-vous avant d'emprunter des objets à la famille ou aux colocataires?",
        "Rendez-vous les objets perdus ou l'argent au propriétaire légitime immédiatement?",
        "Vous présentez-vous aux événements que vous avez promis d'assister, qu'il pleuve ou vente?",
        "Exprimez-vous fréquemment votre gratitude à ceux qui vous rendent service?"
    };

    private static final String[] FRENCH_MODERATE = {
        "Refusez-vous de mentir même si un petit mensonge facilite la conversation?",
        "Admettez-vous vos erreurs immédiatement à votre patron ou collègues?",
        "Dites-vous la vérité à un ami qui demande un retour honnête et difficile?",
        "Refusez-vous de répandre des rumeurs ou des ragots non vérifiés?",
        "Admettez-vous quand vous ne connaissez pas la réponse à une question?",
        "Évitez-vous d'exagérer vos accomplissements sur votre CV?",
        "Écoutez-vous les autres sans interrompre ni juger leurs expériences?",
        "Montrez-vous de la patience envers les travailleurs qui font des erreurs?",
        "Pardonnez-vous aux personnes qui s'excusent sincèrement de vous avoir blessé?",
        "Vous abstenez-vous de vous moquer des gens pour leurs défauts?",
        "Traitez-vous les travailleurs de service avec le même respect que les dirigeants?",
        "Gérez-vous vos dettes personnelles et les remboursez-vous rapidement?",
        "Acceptez-vous les critiques constructives sans devenir défensif?",
        "Suivez-vous les projets de groupe pour ne pas laisser tomber vos partenaires?",
        "Valorisez-vous le temps des autres autant que le vôtre?"
    };

    private static final String[] FRENCH_MEDIUM = {
        "Déclarez-vous votre revenu exact dans vos impôts sans tricher?",
        "Refusez-vous de prendre le crédit pour le travail fait par un collègue?",
        "Tenez-vous vos promesses même quand une meilleure opportunité se présente?",
        "Parlez-vous quand quelqu'un déforme les faits dans une réunion?",
        "Honorez-vous les accords verbaux même sans contrat signé?",
        "Révélez-vous les défauts d'un produit si vous vendez un article d'occasion?",
        "Défendez-vous une personne absente quand de fausses déclarations sont faites?",
        "Essayez-vous activement de comprendre la perspective de gens que vous n'aimez pas?",
        "Jugez-vous les gens uniquement sur leur caractère plutôt que leur origine?",
        "Dénoncez-vous les préjugés systémiques ou la discrimination quand vous en êtes témoin?",
        "Votez-vous pour des politiques bénéfiques pour la société même si elles augmentent vos impôts?",
        "Refusez-vous d'utiliser le népotisme pour avancer dans votre carrière?",
        "Soutenez-vous les réformes de justice pénale visant la réhabilitation?",
        "Maintenez-vous l'éthique professionnelle même si cela risque votre emploi?",
        "Protégez-vous la vie privée et les secrets confidentiels de vos amis?",
        "Gardez-vous les secrets que vos amis vous ont confiés?",
        "Refusez-vous les pots-de-vin destinés à vous faire trahir votre équipe?",
        "Restez-vous fidèle à vos valeurs fondamentales face à l'argent facile?"
    };

    private static final String[] FRENCH_HARD = {
        "Déclarez-vous vos vraies intentions tôt dans une nouvelle relation?",
        "Refusez-vous de disparaître pour éviter une conversation gênante?",
        "Sacrifiez-vous votre temps libre pour aider un ami à déménager?",
        "Donnez-vous régulièrement de l'argent à des causes aidant les pauvres?",
        "Donnez-vous de la nourriture ou de l'argent directement aux sans-abri?",
        "Défendez-vous l'égalité salariale pour un travail égal?",
        "Dénoncez-vous les doubles standards appliqués à différents genres?",
        "Rejetez-vous les privilèges obtenus au détriment direct d'autrui?",
        "Contestez-vous les politiques injustes de votre propre gouvernement?",
        "Démissionnez-vous d'un poste de direction si vous n'êtes plus apte?",
        "Soutenez-vous le droit d'une personne à l'autonomie corporelle?",
        "Soutenez-vous vos amis de longue date quand ils connaissent la pauvreté?",
        "Soutenez-vous votre partenaire pendant de longues périodes de maladie?",
        "Restez-vous aux côtés d'un ami même quand il est socialement impopulaire?",
        "Honorez-vous les accords de confidentialité après avoir quitté une entreprise?"
    };

    private static final String[] FRENCH_EXPERT = {
        "Faites-vous des dons anonymes sans recevoir aucun crédit?",
        "Vous arrêtez-vous pour aider un automobiliste en panne sur une route sombre?",
        "Intervenez-vous pour arrêter une bagarre ou défendre une victime d'intimidation?",
        "Renoncez-vous à votre week-end pour aider aux secours en cas de catastrophe?",
        "Refusez-vous une promotion si un collègue en a beaucoup plus besoin?",
        "Plongez-vous dans le danger pour sortir quelqu'un du péril?",
        "Hébergez-vous quelqu'un qui n'a nulle part ailleurs où aller en sécurité?",
        "Mettez-vous la sécurité des autres avant la vôtre pendant une crise?",
        "Tenez-vous les promesses faites à des personnes décédées?",
        "Protégez-vous les lanceurs d'alerte vulnérables qui vous font confiance?",
        "Retournez-vous aider les communautés qui vous ont élevé ou soutenu?",
        "Tenez-vous votre parole envers vos ennemis ou rivaux sans faillir?",
        "Vous récusez-vous des décisions où vous avez un conflit d'intérêts?"
    };

    // ── AMERICAN PRUDENT ─────────────────────────────────────────────────────

    private static final String[] AMERICAN_PRUDENT_EASY = {
        "Do you inform a merchant who has tendered excessive change in your favor?",
        "Do you hold passage for a person approaching from a reasonable distance?",
        "Do you maintain the cleanliness of shared civic spaces after your use?",
        "Do you observe punctuality in scheduled engagements out of professional courtesy?",
        "Do you exercise sound judgment regarding noise levels in residential areas?",
        "Do you maintain appropriate physical boundaries in public queues?",
        "Do you yield the right of way to merging traffic when prudent?",
        "Do you render assistance to persons who have dropped personal effects?",
        "Do you yield seating on public conveyance to those with greater need?",
        "Do you remove refuse from public thoroughfares not of your making?",
        "Do you request entry before accessing private or restricted spaces?",
        "Do you obtain permission prior to utilizing another person's property?",
        "Do you restore found property to its rightful owner without undue delay?",
        "Do you honor commitments to attend functions regardless of inconvenience?",
        "Do you regularly express appreciation to those who render services to you?"
    };

    private static final String[] AMERICAN_PRUDENT_MODERATE = {
        "Do you maintain truthfulness even when a minor falsehood would ease social friction?",
        "Do you disclose your own errors promptly to those in authority over you?",
        "Do you provide candid assessment when a colleague requests honest evaluation?",
        "Do you decline to propagate unverified allegations regarding third parties?",
        "Do you acknowledge limitations in your knowledge rather than misrepresent your competence?",
        "Do you represent your qualifications accurately in professional documentation?",
        "Do you afford others the courtesy of uninterrupted expression of their views?",
        "Do you exercise forbearance with service personnel who commit good-faith errors?",
        "Do you extend clemency to those who offer genuine contrition for injuries caused?",
        "Do you refrain from disparaging remarks directed at others' personal shortcomings?",
        "Do you accord equal dignity to all persons regardless of their station?",
        "Do you fulfill your financial obligations within their prescribed terms?",
        "Do you receive constructive criticism with professional equanimity?",
        "Do you discharge your obligations in collaborative undertakings without default?",
        "Do you accord proper weight to the temporal commitments of others?"
    };

    private static final String[] AMERICAN_PRUDENT_MEDIUM = {
        "Do you render accurate and complete accounts in your tax filings?",
        "Do you decline to appropriate credit for intellectual contributions made by others?",
        "Do you uphold your covenants even when more advantageous circumstances present?",
        "Do you challenge misstatements of material fact in professional proceedings?",
        "Do you honor verbal undertakings with the same fidelity as written contracts?",
        "Do you disclose known material defects when transferring property to a buyer?",
        "Do you advocate for the reputation of absent persons against false imputation?",
        "Do you exercise due diligence in understanding opposing viewpoints?",
        "Do you evaluate persons on the content of their character absent other bias?",
        "Do you challenge institutional discrimination when it comes to your attention?",
        "Do you support legislation serving the common good despite personal tax burden?",
        "Do you decline to leverage personal connections for unearned advancement?",
        "Do you support restorative approaches within the criminal justice system?",
        "Do you maintain professional standards even at risk to your position?",
        "Do you safeguard privileged communications entrusted to you by associates?",
        "Do you honor confidences placed in you by persons who relied upon your discretion?",
        "Do you reject inducements designed to compromise your professional obligations?",
        "Do you maintain fidelity to your principles when confronted by pecuniary temptation?"
    };

    private static final String[] AMERICAN_PRUDENT_HARD = {
        "Do you disclose your material intentions forthrightly at the inception of relationships?",
        "Do you address interpersonal matters directly rather than through avoidance?",
        "Do you dedicate personal time to assist associates with significant logistical needs?",
        "Do you allocate resources regularly to charitable causes serving the indigent?",
        "Do you provide direct material assistance to persons experiencing homelessness?",
        "Do you advocate for equitable compensation practices within your organization?",
        "Do you challenge disparate treatment based on protected characteristics?",
        "Do you refuse benefits derived from the unjust disadvantage of others?",
        "Do you exercise your civic duty to challenge unjust governmental action?",
        "Do you voluntarily relinquish authority when your capacity to serve is diminished?",
        "Do you respect the right of persons to make autonomous decisions regarding their person?",
        "Do you maintain support for long-standing associates during periods of hardship?",
        "Do you sustain commitment to intimate partners through extended periods of infirmity?",
        "Do you maintain loyalty to associates irrespective of their social standing?",
        "Do you observe confidentiality obligations beyond the termination of employment?"
    };

    private static final String[] AMERICAN_PRUDENT_EXPERT = {
        "Do you provide charitable contributions without expectation of recognition?",
        "Do you render roadside assistance to distressed motorists in hazardous conditions?",
        "Do you intervene to protect persons subject to physical assault or harassment?",
        "Do you dedicate personal time to organized disaster relief operations?",
        "Do you defer professional advancement when a colleague faces more urgent need?",
        "Do you accept personal risk to extract others from imminent physical danger?",
        "Do you provide shelter to persons with no alternative safe accommodation?",
        "Do you subordinate personal safety to protect others during emergencies?",
        "Do you fulfill obligations to the deceased with the same fidelity as to the living?",
        "Do you provide protection to persons who disclose malfeasance in good faith?",
        "Do you render service to communities that contributed to your development?",
        "Do you maintain honor in your dealings even with adversaries and competitors?",
        "Do you recuse yourself from matters presenting conflicts of interest?"
    };

    // ── GERMAN ───────────────────────────────────────────────────────────────

    private static final String[] GERMAN_EASY = {
        "Korrigieren Sie einen Kassierer, der Ihnen zu viel Wechselgeld gibt?",
        "Halten Sie den Aufzug oder die Tür für jemanden, der weit hinter Ihnen geht?",
        "Räumen Sie nach sich selbst in öffentlichen Räumen und Parks auf?",
        "Kommen Sie pünktlich zu Terminen aus Respekt vor anderen?",
        "Halten Sie Ihre Musik in Wohngebieten spät abends leise?",
        "Geben Sie Menschen in öffentlichen Schlangen und Menschenmengen Raum?",
        "Lassen Sie einen anderen Fahrer bei starkem Verkehr einfädeln?",
        "Unterbrechen Sie Ihren Tag, um jemandem beim Aufheben gefallener Gegenstände zu helfen?",
        "Bieten Sie Ihren Sitzplatz im öffentlichen Nahverkehr Bedürftigen an?",
        "Heben Sie Müll auf öffentlichen Gehwegen auf, der nicht Ihrer ist?",
        "Klopfen Sie und warten auf Erlaubnis, bevor Sie private Räume betreten?",
        "Fragen Sie, bevor Sie sich Dinge von Familie oder Mitbewohnern leihen?",
        "Geben Sie gefundene Gegenstände oder Geld sofort dem rechtmäßigen Besitzer zurück?",
        "Erscheinen Sie zu Veranstaltungen, die Sie zugesagt haben, bei Wind und Wetter?",
        "Drücken Sie häufig Dankbarkeit gegenüber denen aus, die Ihnen dienen?"
    };

    private static final String[] GERMAN_MODERATE = {
        "Weigern Sie sich zu lügen, selbst wenn eine kleine Notlüge das Gespräch erleichtert?",
        "Geben Sie Ihre Fehler sofort gegenüber Ihrem Chef oder Kollegen zu?",
        "Sagen Sie einem Freund die Wahrheit, wenn er um ehrliches, schwieriges Feedback bittet?",
        "Weigern Sie sich, Gerüchte oder unbestätigte Geschichten zu verbreiten?",
        "Geben Sie zu, wenn Sie die Antwort auf eine Frage nicht wissen?",
        "Vermeiden Sie es, Ihre Leistungen in Ihrem Lebenslauf zu übertreiben?",
        "Hören Sie anderen zu, ohne zu unterbrechen oder ihre Erfahrungen zu beurteilen?",
        "Zeigen Sie Geduld gegenüber Servicemitarbeitern, die Fehler machen?",
        "Vergeben Sie Menschen, die sich aufrichtig für Verletzungen entschuldigen?",
        "Unterlassen Sie es, Menschen wegen ihrer Schwächen zu verspotten?",
        "Behandeln Sie Servicemitarbeiter mit dem gleichen Respekt wie Führungskräfte?",
        "Verwalten Sie Ihre persönlichen Schulden und zahlen Sie diese pünktlich zurück?",
        "Akzeptieren Sie konstruktives Feedback, ohne defensiv zu werden?",
        "Erfüllen Sie Ihre Aufgaben in Gruppenprojekten, um Partner nicht im Stich zu lassen?",
        "Schätzen Sie die Zeit anderer Menschen genauso wie Ihre eigene?"
    };

    private static final String[] GERMAN_MEDIUM = {
        "Melden Sie Ihr genaues Einkommen bei der Steuer ohne Abkürzungen?",
        "Weigern Sie sich, Anerkennung für die Arbeit eines Kollegen anzunehmen?",
        "Halten Sie Versprechen, auch wenn sich später eine bessere Gelegenheit ergibt?",
        "Sprechen Sie auf, wenn jemand Fakten in einer Besprechung falsch darstellt?",
        "Ehren Sie mündliche Vereinbarungen, auch wenn kein Vertrag unterzeichnet ist?",
        "Offenbaren Sie Produktmängel, wenn Sie einen gebrauchten Artikel verkaufen?",
        "Verteidigen Sie eine abwesende Person, wenn falsche Aussagen über sie gemacht werden?",
        "Versuchen Sie aktiv, die Perspektive von Menschen zu verstehen, die Sie nicht mögen?",
        "Beurteilen Sie Menschen allein nach ihrem Charakter statt nach ihrer Herkunft?",
        "Prangern Sie systemische Vorurteile oder Diskriminierung an, wenn Sie sie beobachten?",
        "Stimmen Sie für Politiken, die der Gesellschaft nutzen, auch wenn sie Ihre Steuern erhöhen?",
        "Weigern Sie sich, Vetternwirtschaft zu nutzen, um in Ihrer Karriere voranzukommen?",
        "Unterstützen Sie Strafjustizreformen, die auf Rehabilitation abzielen?",
        "Halten Sie Berufsethik ein, auch wenn es Ihre Anstellung gefährdet?",
        "Schützen Sie die Privatsphäre und vertraulichen Geheimnisse Ihrer Freunde?",
        "Bewahren Sie Geheimnisse, die Ihnen Freunde anvertraut haben?",
        "Lehnen Sie Bestechungsgelder ab, die Sie dazu bringen sollen, Ihr Team zu verraten?",
        "Bleiben Sie Ihren Grundwerten treu, wenn schnelles Geld lockt?"
    };

    private static final String[] GERMAN_HARD = {
        "Erklären Sie Ihre wahren Absichten früh in einer neuen Beziehung?",
        "Weigern Sie sich, Menschen zu ghosten, um ein unangenehmes Gespräch zu vermeiden?",
        "Opfern Sie Ihre persönliche Freizeit, um einem Freund beim Umzug zu helfen?",
        "Spenden Sie regelmäßig Geld oder Ressourcen an Hilfsorganisationen für Arme?",
        "Geben Sie Obdachlosen direkt Essen oder Geld?",
        "Setzen Sie sich für gleichen Lohn für gleiche Arbeit ein?",
        "Prangern Sie Doppelstandards an, die auf verschiedene Geschlechter angewandt werden?",
        "Lehnen Sie Privilegien ab, die auf direkten Kosten anderer erlangt wurden?",
        "Hinterfragen Sie ungerechte Politiken Ihrer eigenen Regierung?",
        "Treten Sie von der Führung zurück, wenn Sie nicht mehr geeignet sind zu dienen?",
        "Unterstützen Sie das Recht einer Person auf körperliche Autonomie?",
        "Unterstützen Sie langjährige Freunde, wenn sie Armut erleben?",
        "Unterstützen Sie Ihren Partner bei langer chronischer Krankheit?",
        "Stehen Sie zu einem Freund, auch wenn er sozial unbeliebt ist?",
        "Ehren Sie Geheimhaltungsvereinbarungen auch nach Verlassen eines Unternehmens?"
    };

    private static final String[] GERMAN_EXPERT = {
        "Machen Sie anonyme Spenden, bei denen Sie keinerlei Anerkennung erhalten?",
        "Halten Sie an, um einem gestrandeten Autofahrer auf einer dunklen Straße zu helfen?",
        "Greifen Sie ein, um eine Schlägerei zu stoppen oder ein Mobbingopfer zu verteidigen?",
        "Geben Sie Ihr Wochenende auf, um bei Katastrophenhilfe zu unterstützen?",
        "Verzichten Sie auf eine Beförderung, wenn ein Kollege sie dringender braucht?",
        "Stürzen Sie sich in Gefahr, um jemanden aus einer Notlage zu retten?",
        "Beherbergen Sie jemanden, der nirgendwo anders sicher unterkommen kann?",
        "Stellen Sie die Sicherheit anderer während einer Krise über Ihre eigene?",
        "Erfüllen Sie Versprechen, die Sie Verstorbenen gegeben haben?",
        "Schützen Sie verwundbare Whistleblower, die Ihnen Informationen anvertrauen?",
        "Kehren Sie zurück, um Gemeinschaften zu helfen, die Sie aufgezogen haben?",
        "Halten Sie Ihr Wort gegenüber Feinden oder Rivalen ohne Ausnahme?",
        "Treten Sie bei Entscheidungen zurück, bei denen Sie einen Interessenkonflikt haben?"
    };

    // ── GREEK ────────────────────────────────────────────────────────────────

    private static final String[] GREEK_EASY = {
        "Διορθώνετε έναν ταμία που σας δίνει πολλά ρέστα;",
        "Κρατάτε τον ανελκυστήρα ή την πόρτα για κάποιον που περπατάει πολύ πίσω;",
        "Καθαρίζετε μετά τον εαυτό σας σε δημόσιους χώρους και πάρκα;",
        "Φτάνετε στην ώρα σας στα ραντεβού από σεβασμό προς τους άλλους;",
        "Χαμηλώνετε τη μουσική σας σε κατοικημένες περιοχές αργά το βράδυ;",
        "Δίνετε χώρο στους ανθρώπους σε δημόσιες ουρές και πλήθη;",
        "Αφήνετε έναν άλλο οδηγό να ενταχθεί κατά τη διάρκεια βαριάς κυκλοφορίας;",
        "Σταματάτε τη μέρα σας για να βοηθήσετε κάποιον να μαζέψει πεσμένα αντικείμενα;",
        "Προσφέρετε τη θέση σας στα μέσα μαζικής μεταφοράς σε κάποιον που τη χρειάζεται;",
        "Μαζεύετε σκουπίδια στα δημόσια πεζοδρόμια που δεν είναι δικά σας;",
        "Χτυπάτε και περιμένετε άδεια πριν μπείτε σε ιδιωτικούς χώρους;",
        "Ρωτάτε πριν δανειστείτε αντικείμενα από την οικογένεια ή τους συγκατοίκους;",
        "Επιστρέφετε αμέσως χαμένα αντικείμενα ή χρήματα στον νόμιμο ιδιοκτήτη;",
        "Εμφανίζεστε σε εκδηλώσεις που υποσχεθήκατε να παρακολουθήσετε, βρέξει ή χιονίσει;",
        "Εκφράζετε συχνά ευγνωμοσύνη σε αυτούς που σας εξυπηρετούν;"
    };

    private static final String[] GREEK_MODERATE = {
        "Αρνείστε να πείτε ψέματα ακόμα κι αν ένα μικρό ψεματάκι κάνει τη συζήτηση ομαλότερη;",
        "Παραδέχεστε τα λάθη σας αμέσως στον προϊστάμενό σας ή τους συναδέλφους;",
        "Λέτε την αλήθεια σε έναν φίλο όταν ζητάει ειλικρινή, δύσκολη ανατροφοδότηση;",
        "Αρνείστε να διαδίδετε φήμες ή ανεπιβεβαίωτα κουτσομπολιά;",
        "Παραδέχεστε όταν δεν γνωρίζετε την απάντηση σε μια ερώτηση;",
        "Αποφεύγετε να υπερβάλλετε τα επιτεύγματά σας στο βιογραφικό σας;",
        "Ακούτε τους άλλους χωρίς να διακόπτετε ή να κρίνετε τις εμπειρίες τους;",
        "Δείχνετε υπομονή στους εργαζόμενους εξυπηρέτησης που κάνουν λάθη;",
        "Συγχωρείτε ανθρώπους που ζητούν ειλικρινά συγγνώμη που σας πλήγωσαν;",
        "Απέχετε από το να κοροϊδεύετε ανθρώπους για τα ελαττώματά τους;",
        "Αντιμετωπίζετε τους εργαζόμενους εξυπηρέτησης με τον ίδιο σεβασμό όπως τους διευθυντές;",
        "Διαχειρίζεστε τα προσωπικά σας χρέη και τα αποπληρώνετε εγκαίρως;",
        "Αποδέχεστε εποικοδομητική κριτική χωρίς να γίνεστε αμυντικοί;",
        "Ολοκληρώνετε ομαδικά έργα ώστε να μην απογοητεύσετε τους συνεργάτες σας;",
        "Εκτιμάτε τον χρόνο των άλλων όσο εκτιμάτε τον δικό σας;"
    };

    private static final String[] GREEK_MEDIUM = {
        "Δηλώνετε το ακριβές εισόδημά σας στους φόρους χωρίς συντομεύσεις;",
        "Αρνείστε να πάρετε τα εύσημα για δουλειά που έκανε ένας συνάδελφος;",
        "Τηρείτε τις υποσχέσεις σας ακόμα κι όταν μια καλύτερη ευκαιρία προκύψει αργότερα;",
        "Μιλάτε όταν κάποιος παραποιεί γεγονότα σε μια συνάντηση;",
        "Τιμάτε προφορικές συμφωνίες ακόμα κι αν δεν υπάρχει υπογεγραμμένο συμβόλαιο;",
        "Αποκαλύπτετε ελαττώματα προϊόντος αν πουλάτε ένα μεταχειρισμένο αντικείμενο;",
        "Υπερασπίζεστε ένα απόν πρόσωπο όταν γίνονται ψευδείς δηλώσεις εναντίον του;",
        "Προσπαθείτε ενεργά να κατανοήσετε την οπτική ανθρώπων που δεν σας αρέσουν;",
        "Κρίνετε τους ανθρώπους αποκλειστικά βάσει του χαρακτήρα τους;",
        "Καταγγέλλετε συστημική προκατάληψη ή διάκριση όταν τη γίνεστε μάρτυρες;",
        "Ψηφίζετε πολιτικές που ωφελούν την κοινωνία ακόμα κι αν αυξάνουν τους φόρους σας;",
        "Αρνείστε να χρησιμοποιήσετε νεποτισμό για να προχωρήσετε στην καριέρα σας;",
        "Υποστηρίζετε μεταρρυθμίσεις ποινικής δικαιοσύνης που στοχεύουν στην αποκατάσταση;",
        "Διατηρείτε την επαγγελματική ηθική ακόμα κι αν κινδυνεύει η θέση σας;",
        "Προστατεύετε την ιδιωτικότητα και τα εμπιστευτικά μυστικά των φίλων σας;",
        "Κρατάτε μυστικά που σας εμπιστεύτηκαν οι φίλοι σας;",
        "Απορρίπτετε δωροδοκίες που σκοπό έχουν να σας κάνουν να προδώσετε την ομάδα σας;",
        "Παραμένετε πιστοί στις βασικές σας αξίες όταν σας δελεάζει το εύκολο χρήμα;"
    };

    private static final String[] GREEK_HARD = {
        "Δηλώνετε τις αληθινές προθέσεις σας νωρίς σε μια νέα σχέση;",
        "Αρνείστε να εξαφανιστείτε για να αποφύγετε μια δύσκολη συζήτηση;",
        "Θυσιάζετε τον προσωπικό σας ελεύθερο χρόνο για να βοηθήσετε έναν φίλο να μετακομίσει;",
        "Δωρίζετε τακτικά χρήματα ή πόρους σε σκοπούς που βοηθούν τους φτωχούς;",
        "Δίνετε φαγητό ή χρήματα απευθείας σε άστεγα άτομα;",
        "Υποστηρίζετε ίση αμοιβή για ίση εργασία στον χώρο εργασίας σας;",
        "Καταγγέλλετε διπλά πρότυπα που εφαρμόζονται σε διαφορετικά φύλα;",
        "Απορρίπτετε προνόμια που αποκτήθηκαν σε άμεσο κόστος άλλων;",
        "Αμφισβητείτε άδικες πολιτικές που εφαρμόζει η δική σας κυβέρνηση;",
        "Παραιτείστε από ηγετική θέση αν δεν είστε πλέον κατάλληλοι να υπηρετείτε;",
        "Υποστηρίζετε το δικαίωμα του ατόμου στη σωματική αυτονομία;",
        "Υποστηρίζετε τους μακροχρόνιους φίλους σας όταν βιώνουν φτώχεια;",
        "Υποστηρίζετε τον σύντροφό σας κατά τη διάρκεια μακρών χρόνιων ασθενειών;",
        "Στέκεστε δίπλα σε έναν φίλο ακόμα κι όταν είναι κοινωνικά αδημοφίλητος;",
        "Τιμάτε συμφωνίες εμπιστευτικότητας ακόμα και μετά την αποχώρηση από μια εταιρεία;"
    };

    private static final String[] GREEK_EXPERT = {
        "Κάνετε ανώνυμες δωρεές χωρίς να λαμβάνετε απολύτως καμία αναγνώριση;",
        "Σταματάτε για να βοηθήσετε έναν ακινητοποιημένο οδηγό σε σκοτεινό δρόμο;",
        "Επεμβαίνετε για να σταματήσετε έναν καυγά ή να υπερασπιστείτε ένα θύμα εκφοβισμού;",
        "Παραιτείστε από το σαββατοκύριακό σας για να βοηθήσετε σε ανακούφιση καταστροφών;",
        "Παραχωρείτε μια προαγωγή αν ένας συνάδελφος τη χρειάζεται πολύ πιο επειγόντως;",
        "Ρίχνεστε στον κίνδυνο για να βγάλετε κάποιον από βλάβη;",
        "Φιλοξενείτε ή προστατεύετε κάποιον που δεν έχει πουθενά αλλού να πάει με ασφάλεια;",
        "Βάζετε την ασφάλεια των άλλων μπροστά από τη δική σας κατά τη διάρκεια κρίσης;",
        "Εκπληρώνετε υποσχέσεις που δώσατε σε ανθρώπους που έχουν φύγει από τη ζωή;",
        "Προστατεύετε ευάλωτους πληροφοριοδότες που σας εμπιστεύονται πληροφορίες;",
        "Επιστρέφετε για να βοηθήσετε κοινότητες που σας μεγάλωσαν ή σας στήριξαν;",
        "Κρατάτε τον λόγο σας προς τους εχθρούς ή αντιπάλους σας χωρίς εξαίρεση;",
        "Εξαιρείτε τον εαυτό σας από αποφάσεις όπου έχετε σύγκρουση συμφερόντων;"
    };
}
