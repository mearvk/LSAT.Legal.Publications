import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Scanner;

public class Questions006 {
    public static final String[] QUESTIONS = new String[]{
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
    };

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int score = 0;

        System.out.println("Selfless Giving Quiz\nAnswer Y for yes, N for no.\n");

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

        Path outFile = Path.of("src", "questions", "Questions006_score.txt");
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
