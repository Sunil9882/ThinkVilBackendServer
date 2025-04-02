package com.Twitter.Jarvis.ServiceImpl;

import com.Twitter.Jarvis.Model.OrderEntityModel;
import com.Twitter.Jarvis.Repository.OrderRepository;
import com.Twitter.Jarvis.Repository.PaymentRepository;
import com.Twitter.Jarvis.Service.RazorPayService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class RazorPayServiceImpl implements RazorPayService {
    @Value("${razorpay.key}")
    private String key;

    @Value("${razorpay.secret}")
    private String secret;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public OrderEntityModel createOrder(Map<String, Integer> data) throws Exception {
        if (data.get("amount") == null) {
            throw new IllegalArgumentException("Amount is required");
        }

        try {
            RazorpayClient client = new RazorpayClient(key, secret);
            int amount = data.get("amount") * 100; // Convert to paise

            String receipt = "txn_" + System.currentTimeMillis();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount); // Amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", receipt);
            orderRequest.put("payment_capture", 1); // Auto capture payment

            Order razorpayOrder = client.orders.create(orderRequest);

            OrderEntityModel order = new OrderEntityModel();
            order.setRazorpayOrderId(razorpayOrder.get("id"));
            order.setAmount(amount);
            order.setCurrency("INR");
            order.setStatus("CREATED");
            order.setReceipt(receipt);
            order.setCreatedAt(LocalDateTime.now());

            return orderRepository.save(order);

        } catch (RazorpayException e) {
            throw new Exception("Error creating Razorpay order: " + e.getMessage());
        }
    }


    @Override
    public String verifyPayment(Map<String, Object> data) throws Exception {
        try {
            Map<String, Object> paymentData = (Map<String, Object>) data.get("paymentData");
            String orderId = (String) paymentData.get("razorpay_order_id");
            String paymentId = (String) paymentData.get("razorpay_payment_id");
            String razorpaySignature = (String) paymentData.get("razorpay_signature");

            String generatedSignature = hmacSha256(orderId + "|" + paymentId, secret);

            if (generatedSignature.equals(razorpaySignature)) {
                return "Payment Verified Successfully!";
            } else {
                return "Payment Verification Failed!";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


    public void updateOrderStatus(String orderId, String status) {
        OrderEntityModel order = orderRepository.findByRazorpayOrderId(orderId);
        if (order != null) {
            order.setStatus(status);
            orderRepository.save(order);
        }
    }


    private String hmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);

        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Convert bytes to HEX
        StringBuilder hexString = new StringBuilder();
        for (byte b : hmacBytes) {
            hexString.append(String.format("%02x", b)); // Convert to hexadecimal
        }
        return hexString.toString(); // Return HEX string
    }
}
