package questions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Scanner;

public class Questions002 {
    public static final String[] QUESTIONS = new String[]{
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
    };

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int score = 0;

        System.out.println("Compassion & Kindness Quiz\nAnswer Y for yes, N for no.\n");

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

        Path outFile = Path.of("src", "questions", "Questions002_score.txt");
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
