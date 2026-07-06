package com.viatrial.controller;

import com.viatrial.common.Result;
import com.viatrial.dto.request.PaperGenerateRequest;
import com.viatrial.dto.response.PaperGenerateResponse;
import com.viatrial.service.PaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "组卷管理")
@RestController
@RequestMapping("/api/v1/papers")
public class PaperController {

    private final PaperService paperService;

    public PaperController(PaperService paperService) {
        this.paperService = paperService;
    }

    @Operation(summary = "预览试卷")
    @PostMapping("/generate")
    public Result<PaperGenerateResponse> generatePaper(@Valid @RequestBody PaperGenerateRequest request) {
        return Result.success(paperService.generatePaper(request));
    }
}
