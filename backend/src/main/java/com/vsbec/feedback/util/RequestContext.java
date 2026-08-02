package com.vsbec.feedback.util;

import com.vsbec.feedback.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;

/** Small helper to pull JWT-derived attributes set by JwtAuthFilter. */
public class RequestContext {

    public static Long studentId(HttpServletRequest request) {
        Object v = request.getAttribute("jwt_studentId");
        if (v == null) throw ApiException.unauthorized("Not authenticated as student");
        return Long.valueOf(v.toString());
    }

    public static Long adminId(HttpServletRequest request) {
        Object v = request.getAttribute("jwt_adminId");
        if (v == null) throw ApiException.unauthorized("Not authenticated as admin");
        return Long.valueOf(v.toString());
    }
}
