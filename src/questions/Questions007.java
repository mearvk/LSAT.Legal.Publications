import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Scanner;

public class Questions007 {
    public static final String[] QUESTIONS = new String[]{
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
    };

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int score = 0;

        System.out.println("Loyalty & Commitment Quiz\nAnswer Y for yes, N for no.\n");

        for (int i = 0; i < QUESTIONS.length; i++) {
            String q = QUESTIONS[i];
            String prompt = String.format("%02d. %s\n> ", i + 1, q);

            String resp;
            while (true) {
                System.out.print(prompt);
                resp = in.nextLine();
                if (resp == null) resp = "";
                resp = resp.trim().toLowerCase(Locale.ROOT);
                if (resp.isEmpty()) continue;
                if (resp.startsWith("y") || resp.startsWith("n")) break;
                System.out.println("Please answer Y (yes) or N (no).");
            }

            if (resp.startsWith("y")) score++;
        }

        System.out.printf("\nYour score: %d out of %d\n", score, QUESTIONS.length);

        Path outFile = Path.of("src", "questions", "Questions007_score.txt");
        String content = String.format("score=%d\ntotal=%d\n", score, QUESTIONS.length);
        try {
            Files.createDirectories(outFile.getParent());
            Files.writeString(outFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Saved score to " + outFile.toString());
        } catch (IOException e) {
            System.err.println("Failed to save score: " + e.getMessage());
        }

        in.close();
    }
}
