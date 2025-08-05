package com.hei.tsinjo.endpoint.rest.controller.health;

import com.hei.tsinjo.domain.model.Donation;
import com.hei.tsinjo.domain.model.Help;
import com.hei.tsinjo.service.DonationService;
import com.hei.tsinjo.service.HelpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TsinjoController {

    private final DonationService donationService;
    private final HelpService helpService;

    @GetMapping("/")
    public String index(Model model) {
        log.info("Displaying Tsinjo main page");

        // Récupérer tous les dons et aides
        List<Donation> donations = donationService.getAllSuccessfulDonations();
        List<Help> helps = helpService.getAllHelps();

        // Créer une liste combinée pour l'affichage chronologique
        List<HistoryItem> historyItems = new ArrayList<>();

        // Ajouter les donations
        donations.forEach(donation -> {
            historyItems.add(new HistoryItem(
                    "DONATION",
                    donation.getCreatedAt(),
                    donation.getDonor().getFullName(),
                    donation.getDonor().getEmail(),
                    donation.getPayment().getAmount(),
                    donation.getPayment().getPspPaymentId(),
                    donation.getPayment().getPspType().toString(),
                    null // pas de description pour les donations
            ));
        });

        // Ajouter les aides
        helps.forEach(help -> {
            historyItems.add(new HistoryItem(
                    "HELP",
                    help.getCreatedAt(),
                    help.getBeneficiary().getFullName(),
                    help.getBeneficiary().getEmail(),
                    help.getPayment().getAmount(),
                    help.getPayment().getPspPaymentId(),
                    help.getPayment().getPspType().toString(),
                    help.getAccidentDescription()
            ));
        });

        // Trier par ordre anti-chronologique
        historyItems.sort(Comparator.comparing(HistoryItem::getCreatedAt).reversed());

        model.addAttribute("historyItems", historyItems);
        return "index";
    }

    @PostMapping("/donate")
    public String donate(@RequestParam String email,
                         @RequestParam String fullName,
                         @RequestParam String paymentReference,
                         RedirectAttributes redirectAttributes) {

        log.info("Processing donation from: {} with payment reference: {}", email, paymentReference);

        try {
            donationService.createDonation(email, fullName, paymentReference);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Don soumis avec succès ! La vérification du paiement est en cours.");
        } catch (Exception e) {
            log.error("Error processing donation", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors du traitement du don : " + e.getMessage());
        }

        return "redirect:/";
    }

    // Classe pour l'affichage unifié de l'historique
    public static class HistoryItem {
        private final String type;
        private final java.time.Instant createdAt;
        private final String personName;
        private final String personEmail;
        private final Integer amount;
        private final String paymentReference;
        private final String paymentMethod;
        private final String description;

        public HistoryItem(String type, java.time.Instant createdAt, String personName,
                           String personEmail, Integer amount, String paymentReference,
                           String paymentMethod, String description) {
            this.type = type;
            this.createdAt = createdAt;
            this.personName = personName;
            this.personEmail = personEmail;
            this.amount = amount;
            this.paymentReference = paymentReference;
            this.paymentMethod = paymentMethod;
            this.description = description;
        }

        // Getters
        public String getType() { return type; }
        public java.time.Instant getCreatedAt() { return createdAt; }
        public String getPersonName() { return personName; }
        public String getPersonEmail() { return personEmail; }
        public Integer getAmount() { return amount; }
        public String getPaymentReference() { return paymentReference; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getDescription() { return description; }

        public boolean isDonation() { return "DONATION".equals(type); }
        public boolean isHelp() { return "HELP".equals(type); }
    }
}
