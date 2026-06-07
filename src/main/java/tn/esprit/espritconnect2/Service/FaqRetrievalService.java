package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.Entitie.FAQ;
import tn.esprit.espritconnect2.Repository.FAQRepository;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaqRetrievalService {

    private static final Set<String> STOP_WORDS = Set.of(
            "how", "do", "i", "the", "a", "an", "to", "for", "with", "is", "are", "can", "you", "me", "my", "in", "on",
            "what", "where", "when", "why", "who", "does", "did", "will", "would", "could", "should", "about", "help",
            "comment", "faire", "pour", "je", "mon", "ma", "mes", "le", "la", "les", "de", "des", "un", "une", "dans", "en",
            "est", "sont", "que", "qui", "quoi", "avec", "sans", "pas", "plus", "moi", "vous", "ton", "ta", "tes"
    );

    private final FAQRepository faqRepository;
    private final EntityManager entityManager;

    public List<FAQ> findRelevantFaqs(String message, int maxResults) {
        if (message == null || message.isBlank()) {
            return List.of();
        }

        String cleanedQuery = message.toLowerCase(Locale.ROOT)
                .replaceAll("[?,.!;:_()\\-]", " ")
                .trim();

        List<String> keywords = extractKeywords(cleanedQuery);
        LinkedHashSet<FAQ> results = new LinkedHashSet<>();

        if (!keywords.isEmpty()) {
            results.addAll(searchByKeywords(keywords, maxResults));
        }

        if (results.size() < maxResults) {
            List<FAQ> phraseMatches = faqRepository
                    .findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(cleanedQuery, cleanedQuery);
            for (FAQ faq : phraseMatches) {
                results.add(faq);
                if (results.size() >= maxResults) {
                    break;
                }
            }
        }

        if (results.isEmpty() && !keywords.isEmpty()) {
            results.addAll(rankAllFaqsByKeywords(keywords, maxResults));
        }

        return results.stream().limit(maxResults).collect(Collectors.toList());
    }

    public String buildKnowledgeContext(List<FAQ> faqs) {
        if (faqs.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (FAQ faq : faqs) {
            sb.append("[KB-").append(i++).append("]\n");
            sb.append("Question: ").append(faq.getQuestion()).append("\n");
            sb.append("Answer: ").append(faq.getAnswer()).append("\n");
            if (faq.getCategory() != null) {
                sb.append("Category: ").append(faq.getCategory().getName()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private List<String> extractKeywords(String cleanedQuery) {
        return Arrays.stream(cleanedQuery.split("\\s+"))
                .filter(w -> w.length() > 2 && !STOP_WORDS.contains(w))
                .distinct()
                .collect(Collectors.toList());
    }

    private List<FAQ> searchByKeywords(List<String> keywords, int maxResults) {
        StringBuilder jql = new StringBuilder(
                "SELECT DISTINCT f FROM FAQ f LEFT JOIN FETCH f.category WHERE ");
        List<String> clauses = new ArrayList<>();
        for (int i = 0; i < keywords.size(); i++) {
            clauses.add("(LOWER(f.question) LIKE :word" + i + " OR LOWER(f.answer) LIKE :word" + i + ")");
        }
        jql.append(String.join(" OR ", clauses));

        var query = entityManager.createQuery(jql.toString(), FAQ.class);
        for (int i = 0; i < keywords.size(); i++) {
            query.setParameter("word" + i, "%" + keywords.get(i) + "%");
        }
        query.setMaxResults(maxResults);
        return query.getResultList();
    }

    private List<FAQ> rankAllFaqsByKeywords(List<String> keywords, int maxResults) {
        return faqRepository.findAll().stream()
                .sorted(Comparator.comparingInt(f -> -scoreFaq(f, keywords)))
                .filter(f -> scoreFaq(f, keywords) > 0)
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    private int scoreFaq(FAQ faq, List<String> keywords) {
        String text = (faq.getQuestion() + " " + faq.getAnswer()).toLowerCase(Locale.ROOT);
        int score = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                score++;
            }
        }
        return score;
    }
}
