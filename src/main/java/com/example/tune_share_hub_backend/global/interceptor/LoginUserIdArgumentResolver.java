package com.example.tune_share_hub_backend.global.interceptor;

import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;



@Component
public class LoginUserIdArgumentResolver implements HandlerMethodArgumentResolver {
    // 이 Resolver가 현재 파라미터를 처리할 수 있는지 검사
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // LoginUserId 어노테이션이 붙었는지 확인하고 Long 타입인지 확인
        return parameter.hasParameterAnnotation(LoginUserId.class)
                && (parameter.getParameterType().equals(Long.class)
                || parameter.getParameterType().equals(long.class));
    }

    // 위 검사를 통과했다면 실제 파라미터에 주입할 값을 반환
    // MethodParameter: 현재 메서드의 파라미터 정보
    // ModelAndViewContainer: 모델과 뷰 정보를 담는 컨테이너 (View 렌더링 관련 객체로 REST API에서는 거의 사용 안 함)
    // NativeWebRequest: 현재 웹 요청 정보(실제 HttpServletRequest를 가져옴)
    // WebDataBinderFactory: 데이터 바인딩을 위한 팩토리(현재 코드에서 사용하지 않음)
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request != null) {
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr instanceof Long userId) {
                return userId;
            }
        }

        throw new CustomException(ErrorCode.AUTH_CONTEXT_NOT_FOUND);
    }
}
