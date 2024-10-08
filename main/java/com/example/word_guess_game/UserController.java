package com.example.word_guess_game;


import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private WordRepository wordRepository;

    @GetMapping("/dashboard")
    public String showForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        String winMessage = (String) session.getAttribute("win_message");
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("levels", new String[]{"Easy", "Medium", "Hard"});
            model.addAttribute("selectedLevel", "");
            model.addAttribute("message", winMessage);
            return "word-form";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/word")
    public String getWord(@ModelAttribute("selectedLevel") String selectedLevel,
                          Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        session.setAttribute("win_message", null);
        if (user != null) {
            Word word = wordRepository.findRandomWordByLevel(selectedLevel);
            model.addAttribute("word", word);
            session.setAttribute("word", word);
            session.setAttribute("attempts_left", 3); // Initialize attempts counter
            model.addAttribute("user", user);
            return "redirect:/showWord";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/showWord")
    public String showWord(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            Word word = (Word) session.getAttribute("word");
            model.addAttribute("GivenHints", word.getHints());
            model.addAttribute("GivenImage", word.getImage());
            model.addAttribute("user", user);
            return "word-input";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/submitWord")
    public String checkWord(@RequestParam String letter1,
                            @RequestParam String letter2,
                            @RequestParam String letter3,
                            @RequestParam String letter4,
                            @RequestParam String letter5,
                            HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            Word wordArray = (Word) session.getAttribute("word");
            Integer attemptsLeft = (Integer) session.getAttribute("attempts_left");

            // Concatenate the letters from the input fields
            String guessedWord = (letter1 + letter2 + letter3 + letter4 + letter5).toLowerCase();
            String correctWord = wordArray.getWordName().toLowerCase();

            model.addAttribute("GivenHints", wordArray.getHints());
            model.addAttribute("GivenImage", wordArray.getImage());

            // Check if the concatenated word matches the correct word
            if (guessedWord.equals(correctWord)) {
                model.addAttribute("message", "Congratulations! You win");
                Integer score = Math.toIntExact(user.getScore() + 10);
                return "redirect:/Score/" + user.getId() + "/" + score;
            } else {
                // Decrement attempts left and check if user has any attempts remaining
                attemptsLeft--;
                session.setAttribute("attempts_left", attemptsLeft);

                if (attemptsLeft > 0) {
                    model.addAttribute("message", "Incorrect! You have " + attemptsLeft + " attempt(s) left.");
                } else {
                    model.addAttribute("message", "Sorry! You lose. No more attempts left.");
                    return "redirect:/dashboard"; // or any end screen you want
                }

                return "word-input";
            }
        } else {
            return "redirect:/";
        }
    }
}