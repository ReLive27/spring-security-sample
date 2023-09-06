package com.relive27.mfa.convert;

import com.relive27.mfa.handler.MfaAuthenticationResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: ReLive27
 * @date: 2023/1/13 19:39
 */
public class MfaAuthenticationHttpMessageConverter extends AbstractHttpMessageConverter<MfaAuthenticationResponse> {
    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<Map<String, Object>>() {
    };

    private final GenericHttpMessageConverter<Object> jsonMessageConverter = new MappingJackson2HttpMessageConverter();
    private Converter<MfaAuthenticationResponse, Map<String, Object>> converter = new MfaAuthenticationResponseMapConverter();


    @Override
    protected boolean supports(Class<?> clazz) {
        return MfaAuthenticationResponse.class.isAssignableFrom(clazz);
    }

    @Override
    protected MfaAuthenticationResponse readInternal(Class<? extends MfaAuthenticationResponse> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException(
                "Result is empty when reading MfaAuthenticationResponse", inputMessage);
    }

    @Override
    protected void writeInternal(MfaAuthenticationResponse mfaAuthenticationResponse, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try {
            Map<String, Object> mfaResponseParameters =
                    this.converter.convert(mfaAuthenticationResponse);
            this.jsonMessageConverter.write(
                    mfaResponseParameters,
                    STRING_OBJECT_MAP.getType(),
                    MediaType.APPLICATION_JSON,
                    outputMessage
            );
        } catch (Exception ex) {
            throw new HttpMessageNotWritableException(
                    "An error occurred writing the MFA login response: " + ex.getMessage(), ex);
        }
    }

    private static final class MfaAuthenticationResponseMapConverter implements Converter<MfaAuthenticationResponse, Map<String, Object>> {

        @Override
        public Map<String, Object> convert(MfaAuthenticationResponse source) {
            Map<String, Object> responseClaims = new LinkedHashMap<>();
            responseClaims.put("code", source.getResponseCode());
            responseClaims.put("message", source.getMessage());
            Map<String, Object> data = new HashMap<>();
            data.put("mfa", source.getMfa());
            if (StringUtils.hasText(source.getQrCode())) {
                data.put("qrCode", source.getQrCode());
            }
            if (StringUtils.hasText(source.getToken())) {
                data.put("token", source.getToken());
            }
            responseClaims.put("data", data);
            return responseClaims;
        }
    }
}
