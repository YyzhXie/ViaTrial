package com.viatrial.service;

import com.viatrial.dto.request.PaperGenerateRequest;
import com.viatrial.dto.response.PaperGenerateResponse;

public interface PaperService {

    PaperGenerateResponse generatePaper(PaperGenerateRequest request);
}
