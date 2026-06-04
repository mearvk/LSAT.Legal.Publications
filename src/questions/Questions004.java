import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Scanner;

public class Questions004 {
    public static final String[] QUESTIONS = new String[]{
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
    };

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int score = 0;

        System.out.println("Responsibility & Duty Quiz\nAnswer Y for yes, N for no.\n");

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

        Path outFile = Path.of("src", "questions", "Questions004_score.txt");
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
