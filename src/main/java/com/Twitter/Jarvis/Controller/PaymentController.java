package com.Twitter.Jarvis.Controller;

import com.Twitter.Jarvis.Model.OrderEntityModel;
import com.Twitter.Jarvis.Service.RazorPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/payment/")
public class PaymentController {

    @Autowired
    private RazorPayService razorpayService;

    @PostMapping("create-order")
    OrderEntityModel createOrder(@RequestBody Map<String, Integer> data) {
        try {
            return razorpayService.createOrder(data);
        } catch (Exception e) {
            throw new RuntimeException("Order creation failed: " + e.getMessage());
        }
    }

    @PostMapping("verify-payment")
    public String verifyPayment(@RequestBody Map<String, Object> data) {
        try {
            return razorpayService.verifyPayment(data);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
