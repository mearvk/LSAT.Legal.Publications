import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Scanner;

public class Questions003 {
    public static final String[] QUESTIONS = new String[]{
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
    };

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int score = 0;

        System.out.println("Fairness & Equity Quiz\nAnswer Y for yes, N for no.\n");

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

        Path outFile = Path.of("src", "questions", "Questions003_score.txt");
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
