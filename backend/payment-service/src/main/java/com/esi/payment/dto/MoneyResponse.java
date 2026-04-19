package com.esi.payment.dto;

import java.math.BigDecimal;

public record MoneyResponse(BigDecimal amount, String currency) {}
