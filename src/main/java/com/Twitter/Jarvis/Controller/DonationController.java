package com.Twitter.Jarvis.Controller;

import com.Twitter.Jarvis.Model.PaymentModel;
import com.Twitter.Jarvis.Service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/donation")
public class DonationController {

    @Autowired
    private DonationService donationService;

    @PostMapping("/save")
    public PaymentModel createPayment(@RequestBody PaymentModel paymentModel) {
        try {
            return donationService.donationSave(paymentModel);
        } catch (Exception e) {
            return null;
        }
    }
}
