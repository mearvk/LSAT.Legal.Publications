import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Scanner;

public class Questions001 {
    public static final String[] QUESTIONS = new String[]{
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
    };

    // Program runs a simple terminal quiz: Y/Yes counts as 1, others count as 0.
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int score = 0;

        System.out.println("Honesty & Integrity Quiz\nAnswer Y for yes, N for no.\n");

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

        // Save score to a local file relative to source: src/questions/Questions001_score.txt
        Path outFile = Path.of("src", "questions", "Questions001_score.txt");
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
