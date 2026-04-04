package guesthouse.common.config;

import guesthouse.common.annotation.UserId;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

@Component
public class UserIdParameterCustomizer implements ParameterCustomizer {

    @Override
    public Parameter customize(Parameter parameterModel, MethodParameter methodParameter) {
        if (methodParameter.hasParameterAnnotation(UserId.class)) {
            parameterModel.setRequired(false);
            parameterModel.addExtension("x-hidden", true);
            return null;
        }
        return parameterModel;
    }
}
