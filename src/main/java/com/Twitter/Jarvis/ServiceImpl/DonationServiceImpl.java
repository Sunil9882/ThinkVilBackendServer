package com.Twitter.Jarvis.ServiceImpl;

import com.Twitter.Jarvis.Dto.DonationServiceDto;
import com.Twitter.Jarvis.Model.PaymentModel;
import com.Twitter.Jarvis.Repository.PaymentRepository;
import com.Twitter.Jarvis.Service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DonationServiceImpl implements DonationService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Override
    public PaymentModel donationSave(DonationServiceDto donationServiceDto) throws Exception {
        try {
            PaymentModel paymentModel = new PaymentModel();
            paymentModel.setFullName(donationServiceDto.getFullName());
            paymentModel.setAmount(donationServiceDto.getAmount());
            paymentModel.setEmail(donationServiceDto.getEmail());
            paymentModel.setPaymentId(donationServiceDto.getPaymentId());
            paymentModel.setCountry(donationServiceDto.getCountry());
            paymentModel.setState(donationServiceDto.getState());
            return paymentRepository.save(paymentModel);
        } catch (Exception e) {
            return null;
        }
    }
}
