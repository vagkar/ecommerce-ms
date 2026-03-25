package com.ecommerce.user.dto;

public record AuthResponse(String token, String email, String role) {}