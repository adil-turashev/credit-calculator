package org.example.credit4.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.credit4.dto.CreditForm;
import org.example.credit4.dto.ResultDto;
import org.example.credit4.service.CreditService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @GetMapping("/")
    public String index() {
        return "home";
    }

    @GetMapping("/calculator")
    public String calculator(
            Model model,
            Principal principal,
            @CookieValue(value = "user_id", required = false) String userId,
            HttpServletResponse response
    ) {
        String ownerKey = resolveOwnerKey(principal, userId, response);

        model.addAttribute("creditForm", new CreditForm());
        model.addAttribute("myRequests", creditService.getRequestsByOwnerKey(ownerKey));
        return "index";
    }

    @PostMapping("/calculate")
    public String calculate(
            @Valid @ModelAttribute("creditForm") CreditForm creditForm,
            BindingResult bindingResult,
            Principal principal,
            @CookieValue(value = "user_id", required = false) String userId,
            HttpServletResponse response,
            Model model
    ) {
        String ownerKey = resolveOwnerKey(principal, userId, response);

        if (bindingResult.hasErrors()) {
            model.addAttribute("myRequests", creditService.getRequestsByOwnerKey(ownerKey));
            return "index";
        }

        ResultDto result = creditService.calculateAndSave(creditForm, ownerKey);
        model.addAttribute("result", result);
        return "result";
    }

    @GetMapping("/history")
    public String history(
            Principal principal,
            @CookieValue(value = "user_id", required = false) String userId,
            HttpServletResponse response,
            Model model
    ) {
        String ownerKey = resolveOwnerKey(principal, userId, response);

        model.addAttribute("requests", creditService.getRequestsByOwnerKey(ownerKey));
        return "history";
    }

    @GetMapping("/requests")
    public String allRequests(Model model) {
        model.addAttribute("requests", creditService.getAllRequest());
        return "table";
    }

    private String resolveOwnerKey(Principal principal, String userId, HttpServletResponse response) {
        if (principal != null) {
            return principal.getName();
        }

        if (userId == null || userId.isBlank()) {
            userId = UUID.randomUUID().toString();
            Cookie cookie = new Cookie("user_id", userId);
            cookie.setMaxAge(60 * 60 * 24 * 30);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        return userId;
    }
}