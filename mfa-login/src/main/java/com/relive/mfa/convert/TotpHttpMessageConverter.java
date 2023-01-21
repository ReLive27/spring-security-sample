package com.relive.mfa.convert;

import com.relive.mfa.handler.TotpMfaResponse;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: ReLive
 * @date: 2023/1/13 19:39
 */
public class TotpHttpMessageConverter extends AbstractHttpMessageConverter<TotpMfaResponse> {
    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<Map<String, Object>>() {
    };

    private final GenericHttpMessageConverter<Object> jsonMessageConverter = new MappingJackson2HttpMessageConverter();
    private Converter<TotpMfaResponse, Map<String, Object>> totpResponseConverter = new TotpResponseMapConverter();


    @Override
    protected boolean supports(Class<?> clazz) {
        return TotpMfaResponse.class.isAssignableFrom(clazz);
    }

    @Override
    protected TotpMfaResponse readInternal(Class<? extends TotpMfaResponse> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException(
                "Result is empty when reading TotpMfaResponse", inputMessage);
    }

    @Override
    protected void writeInternal(TotpMfaResponse totpMfaResponse, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try {
            Map<String, Object> totpMfaResponseParameters =
                    this.totpResponseConverter.convert(totpMfaResponse);
            this.jsonMessageConverter.write(
                    totpMfaResponseParameters,
                    STRING_OBJECT_MAP.getType(),
                    MediaType.APPLICATION_JSON,
                    outputMessage
            );
        } catch (Exception ex) {
            throw new HttpMessageNotWritableException(
                    "An error occurred writing the MFA login response: " + ex.getMessage(), ex);
        }
    }

    private static final class TotpResponseMapConverter implements Converter<TotpMfaResponse, Map<String, Object>> {

        @Override
        public Map<String, Object> convert(TotpMfaResponse source) {
            Map<String, Object> responseClaims = new LinkedHashMap<>();
            responseClaims.put("message", source.getMessage());
            responseClaims.put("mfa", source.getMfa());
            if (StringUtils.hasText(source.getQrCode())) {
                responseClaims.put("qrCode", source.getQrCode());
            }
            if (StringUtils.hasText(source.getToken())) {
                responseClaims.put("token", source.getToken());
            }
            return responseClaims;
        }
    }
}
