package com.hotel.dto;

public record ApiResponse<T>(String message, T data) {}
