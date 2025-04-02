package com.Twitter.Jarvis.Service;

import com.Twitter.Jarvis.Dto.DonationServiceDto;
import com.Twitter.Jarvis.Model.PaymentModel;

public interface DonationService {
    PaymentModel donationSave(DonationServiceDto donationServiceDto) throws Exception;
}
