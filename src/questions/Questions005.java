import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Scanner;

public class Questions005 {
    public static final String[] QUESTIONS = new String[]{
        "Do you respect someone’s choice to say \"no\" without pressuring them?",
        "Do you protect the privacy and confidential secrets of your friends?",
        "Do you ask for explicit consent before touching or hugging someone?",
        "Do you use people’s preferred names and pronouns correctly?",
        "Do you allow your children or dependents to choose their own career path?",
        "Do you knock and wait for permission before entering private rooms?",
        "Do you avoid tracking or snooping on your partner's phone and messages?",
        "Do you value other people's time as much as you value your own?",
        "Do you listen quietly to religious or philosophical views different from yours?",
        "Do you refrain from making fun of people’s cultural traditions?",
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
    };

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int score = 0;

        System.out.println("Dignity & Boundaries Quiz\nAnswer Y for yes, N for no.\n");

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

        Path outFile = Path.of("src", "questions", "Questions005_score.txt");
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
