package hello.type_converter.formatter;

import hello.type_converter.converter.IpPortToStringConverter;
import hello.type_converter.converter.StringToIpPortConverter;
import hello.type_converter.type.IpPort;
import org.junit.jupiter.api.Test;
import org.springframework.format.support.DefaultFormattingConversionService;

import static org.assertj.core.api.Assertions.assertThat;

class FormattingConversionServiceTest {
    @Test
    void formattingConversionService() {
        // when
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        // 컨버터 등록
        conversionService.addConverter(new StringToIpPortConverter());
        conversionService.addConverter(new IpPortToStringConverter());
        // 포멧터 등록
        conversionService.addFormatter(new MyNumberFormatter());

        // then
        IpPort ipPort = conversionService.convert("127.0.0.1:8080", IpPort.class);
        String convert1 = conversionService.convert(1000, String.class);
        Long convert2 = conversionService.convert("1,000", Long.class);

        // given
        assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));
        assertThat(convert1).isEqualTo("1,000");
        assertThat(convert2).isEqualTo(1000L);
    }
}
