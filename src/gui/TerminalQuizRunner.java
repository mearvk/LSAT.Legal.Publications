package gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Scanner;

/**
 * TerminalQuizRunner — Console-based quiz runner launched when the user selects
 * "Terminal" mode from the startup dialog. Mirrors the GUI quiz flow but operates
 * entirely via stdin/stdout.
 */
public class TerminalQuizRunner {

    private static final String[][] ALL_QUIZZES = {
        // Questions001 — Honesty & Integrity
        {
            "What is the primary legal issue in a negligence case?",
            "Do you refuse to lie even if a small white lie makes a conversation smoother?",
            "Do you admit your mistakes immediately to your boss or peers?",
            "Do you correct a cashier who gives you too much change back?",
            "Do you tell a friend the truth when they ask for honest, difficult feedback?",
            "Do you report your exact income on your taxes without cutting corners?",
            "Do you own up to breaking something in a public or shared space?",
            "Do you refuse to take credit for work done by a colleague?",
            "Do you keep promises even when a better opportunity arises later?",
            "Do you speak up when someone misrepresents facts in a meeting?",
            "Do you return lost items or money to the rightful owner immediately?",
            "Do you avoid exaggerating your accomplishments on your resume?",
            "Do you tell your partner about a major mistake you made?",
            "Do you refuse to spread rumors or unverified gossip?",
            "Do you admit when you do not know the answer to a question?",
            "Do you honor verbal agreements even if no contract is signed?",
            "Do you reveal product flaws if you are selling a used item?",
            "Do you state your true intentions early in a new relationship?",
            "Do you pay for items you accidentally walked out of a store with?",
            "Do you defend an absent person when false statements are made about them?",
            "Do you refuse to ghost people to avoid an awkward conversation?"
        },
        // Questions002 — Compassion & Kindness
        {
            "Do you actively comfort a stranger or colleague who is visibly upset?",
            "Do you sacrifice your personal free time to help a friend move?",
            "Do you regularly donate money or resources to causes helping the poor?",
            "Do you listen to others without interrupting or judging their experiences?",
            "Do you go out of your way to assist an elderly person?",
            "Do you show patience to customer service workers who make mistakes?",
            "Do you actively try to understand the perspective of people you dislike?",
            "Do you feed or care for stray or abandoned animals?",
            "Do you check in on friends experiencing grief or personal tragedy?",
            "Do you forgive people who genuinely apologize for hurting you?",
            "Do you offer your seat on public transit to someone who needs it?",
            "Do you give food or money directly to homeless individuals?",
            "Do you pause your day to help someone pick up dropped items?",
            "Do you refrain from mocking people for their flaws or insecurities?",
            "Do you show genuine joy for other people's successes and milestones?",
            "Do you support colleagues who are struggling with their workloads?",
            "Do you advocate for family members who cannot speak for themselves?",
            "Do you express gratitude frequently to those who provide you service?",
            "Do you notice and include people who are left out of conversations?",
            "Do you treat people with kindness even when you are having a bad day?"
        },
        // Questions003 — Fairness & Equity
        {
            "Do you judge people solely on their character rather than their background?",
            "Do you advocate for equal pay for equal work in your workplace?",
            "Do you support rules that protect minority voices in group decisions?",
            "Do you divide shared resources or bills exactly down the middle?",
            "Do you accept a fair punishment when you violate an established rule?",
            "Do you call out systemic bias or discrimination when you witness it?",
            "Do you give equal attention to all your children or subordinates?",
            "Do you vote for policies that benefit society even if they raise your taxes?",
            "Do you insist on taking turns in long lines or heavy traffic?",
            "Do you refuse to use nepotism to get ahead in your career?",
            "Do you judge arguments based on facts rather than who is speaking?",
            "Do you support criminal justice reforms aimed at rehabilitating people?",
            "Do you recuse yourself from decisions where you have a conflict of interest?",
            "Do you ensure everyone gets credit during collaborative team projects?",
            "Do you call out double standards when applied to different genders?",
            "Do you give consumers or clients a fair price for your services?",
            "Do you listen to both sides of a dispute before forming an opinion?",
            "Do you reject privileges gained at the direct expense of others?",
            "Do you challenge unfair policies implemented by your own government?",
            "Do you treat competitors with professional respect and equity?"
        },
        // Questions004 — Responsibility & Duty
        {
            "Do you accept the consequences of a failed project without blaming others?",
            "Do you clean up after yourself in public spaces and parks?",
            "Do you fulfill promises made to children or dependents reliably?",
            "Do you arrive on time for appointments out of respect for others?",
            "Do you manage your personal debts and pay them back promptly?",
            "Do you vote in local and national elections as a civic duty?",
            "Do you follow safety regulations even when no one is watching you?",
            "Do you report hazardous conditions in your neighborhood to authorities?",
            "Do you take care of borrowed property and return it in top condition?",
            "Do you step down from leadership if you are no longer fit to serve?",
            "Do you honor your commitments to community or volunteer organizations?",
            "Do you accept constructive feedback without becoming defensive?",
            "Do you fix your mistakes rather than trying to hide them?",
            "Do you take responsibility for your physical and mental health choices?",
            "Do you respect the boundaries and limits set by your loved ones?",
            "Do you minimize your personal waste and consumer footprint daily?",
            "Do you care for aging parents or relatives when they need support?",
            "Do you uphold professional ethics even if it risks your employment?",
            "Do you review your past actions to find ways to be a better person?",
            "Do you complete tasks assigned to you thoroughly and without shortcuts?"
        },
        // Questions005 — Dignity & Boundaries
        {
            "Do you respect someone's choice to say \"no\" without pressuring them?",
            "Do you protect the privacy and confidential secrets of your friends?",
            "Do you ask for explicit consent before touching or hugging someone?",
            "Do you use people's preferred names and pronouns correctly?",
            "Do you allow your children or dependents to choose their own career path?",
            "Do you knock and wait for permission before entering private rooms?",
            "Do you avoid tracking or snooping on your partner's phone and messages?",
            "Do you value other people's time as much as you value your own?",
            "Do you listen quietly to religious or philosophical views different from yours?",
            "Do you refrain from making fun of people's cultural traditions?",
            "Do you keep your music down in residential areas late at night?",
            "Do you respect property lines and neighborly boundaries diligently?",
            "Do you give people physical space in public lines and crowds?",
            "Do you allow others to express their emotions without policing them?",
            "Do you treat service workers with the same respect as executives?",
            "Do you support a person's right to bodily autonomy and medical choice?",
            "Do you refrain from using condescending language during disagreements?",
            "Do you ask before borrowing items from family or roommates?",
            "Do you respect an individual's decision to cut ties with toxic people?",
            "Do you acknowledge and honor the quiet enjoyment of shared spaces?"
        },
        // Questions006 — Selfless Giving
        {
            "Do you give anonymous donations where you receive absolutely no credit?",
            "Do you pull over to help a stranded motorist on a dark highway?",
            "Do you regularly donate blood or register as an organ donor?",
            "Do you step in to break up a fight or defend a victim of bullying?",
            "Do you share your food with someone who has none to eat?",
            "Do you give up your weekend to assist with disaster relief efforts?",
            "Do you run errands for a sick neighbor without asking for money?",
            "Do you take the smallest portion of food to leave more for others?",
            "Do you mentor younger people in your field without charging a fee?",
            "Do you let another driver merge into your lane during heavy traffic?",
            "Do you hold the elevator or door for someone walking far behind you?",
            "Do you volunteer at a local soup kitchen or homeless shelter?",
            "Do you give away useful household items to families in need for free?",
            "Do you stay late to help clean up after a party or community event?",
            "Do you pass up a promotion if a colleague needs it far more urgently?",
            "Do you dive into danger to pull someone out of harm's way?",
            "Do you offer free professional advice to people who cannot afford it?",
            "Do you pick up litter on public sidewalks that is not yours?",
            "Do you host or shelter someone who has nowhere else to go safely?",
            "Do you put the safety of others ahead of your own during a crisis?"
        },
        // Questions007 — Loyalty & Commitment
        {
            "Do you stand by a friend even when they are socially unpopular?",
            "Do you keep sensitive corporate data safe from external competitors?",
            "Do you stay faithful to your romantic partner throughout the relationship?",
            "Do you defend your family members against unfair external attacks?",
            "Do you keep secrets that your friends trusted you to hold?",
            "Do you show up to events you promised to attend, rain or shine?",
            "Do you support your long-term friends when they experience poverty?",
            "Do you fulfill promises made to people who have passed away?",
            "Do you stick with a struggling team or company you committed to help?",
            "Do you speak positively about your close friends behind their backs?",
            "Do you refuse bribes intended to make you betray your team?",
            "Do you follow through on group projects so you do not let partners down?",
            "Do you maintain professional boundaries with your clients at all times?",
            "Do you preserve family traditions that hold deep moral meaning?",
            "Do you protect vulnerable whistleblowers who trust you with info?",
            "Do you remain loyal to your core values when tempted by fast money?",
            "Do you support your partner through long periods of chronic illness?",
            "Do you honor non-disclosure agreements even after leaving a company?",
            "Do you return to help communities that raised or supported you?",
            "Do you keep your word to your enemies or rivals without fail?"
        }
    };

    private static final String[] QUIZ_TITLES = {
        "Honesty & Integrity",
        "Compassion & Kindness",
        "Fairness & Equity",
        "Responsibility & Duty",
        "Dignity & Boundaries",
        "Selfless Giving",
        "Loyalty & Commitment"
    };

    private static final String[] QUIZ_IDS = {
        "Questions001", "Questions002", "Questions003", "Questions004",
        "Questions005", "Questions006", "Questions007"
    };

    /**
     * Run the terminal-based quiz session. Displays a menu to pick a section,
     * then runs the Y/N quiz, applies the default normal-distribution curve,
     * and prints both raw and curved results.
     */
    public static void run() {
        Scanner in = new Scanner(System.in);
        MoralCurveFilter curve = new MoralCurveFilter(); // default normal distribution

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   LSAT Moral Assessment — Terminal Mode      ║");
        System.out.println("║   © 2026 Max Rupplin — All Rights Reserved   ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();

        while (true) {
            System.out.println("Select a quiz section (or Q to quit):");
            System.out.println("─────────────────────────────────────────");
            for (int i = 0; i < QUIZ_TITLES.length; i++) {
                System.out.printf("  %d. %s (%d questions)%n",
                        i + 1, QUIZ_TITLES[i], ALL_QUIZZES[i].length);
            }
            System.out.println("  Q. Quit");
            System.out.print("\n> ");

            String sel = in.nextLine().trim().toLowerCase(Locale.ROOT);
            if (sel.equals("q") || sel.equals("quit")) {
                System.out.println("\nGoodbye.");
                break;
            }

            int idx;
            try {
                idx = Integer.parseInt(sel) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection. Try again.\n");
                continue;
            }
            if (idx < 0 || idx >= QUIZ_TITLES.length) {
                System.out.println("Invalid selection. Try again.\n");
                continue;
            }

            // Run the selected quiz
            runQuiz(in, idx, curve);
            System.out.println();
        }

        in.close();
    }

    private static void runQuiz(Scanner in, int quizIndex, MoralCurveFilter curve) {
        String[] questions = ALL_QUIZZES[quizIndex];
        String title = QUIZ_TITLES[quizIndex];
        String id = QUIZ_IDS[quizIndex];

        System.out.printf("%n═══ %s ═══%n", title);
        System.out.println("Answer Y for yes, N for no.\n");

        boolean[] answers = new boolean[questions.length];
        int rawScore = 0;

        for (int i = 0; i < questions.length; i++) {
            String resp;
            while (true) {
                System.out.printf("%02d. %s%n> ", i + 1, questions[i]);
                resp = in.nextLine();
                if (resp == null) resp = "";
                resp = resp.trim().toLowerCase(Locale.ROOT);
                if (resp.isEmpty()) continue;
                if (resp.startsWith("y") || resp.startsWith("n")) break;
                System.out.println("Please answer Y (yes) or N (no).");
            }

            boolean yes = resp.startsWith("y");
            answers[i] = yes;
            if (yes) rawScore++;
        }

        // Calculate scores
        double rawPct = (double) rawScore / questions.length * 100.0;
        double curvedPct = curve.getCurvedPercentage(answers);
        double curvedScore = curve.applyCurve(answers);
        double maxCurved = curve.getMaxScore(questions.length);

        System.out.println("\n─────────────────────────────────────────");
        System.out.printf("Raw Score:    %d / %d (%.1f%%)%n", rawScore, questions.length, rawPct);
        System.out.printf("Curved Score: %.2f / %.2f (%.1f%%)%n", curvedScore, maxCurved, curvedPct);
        System.out.println("─────────────────────────────────────────");

        // Save to file
        Path outFile = Path.of("src", "questions", id + "_score.txt");
        String content = String.format("score=%d\ntotal=%d\ncurved_percent=%.2f\ncurve_config=%s\n",
                rawScore, questions.length, curvedPct, java.util.Arrays.toString(curve.getWeights()));
        try {
            Files.createDirectories(outFile.getParent());
            Files.writeString(outFile, content,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Saved score to " + outFile);
        } catch (IOException e) {
            System.err.println("Failed to save score: " + e.getMessage());
        }
    }
}
