package com.cropdeal.order;
import com.cropdeal.order.service.QrCodeService; import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class QrCodeServiceTest {
 @Test void generatesPngQrCode(){byte[] png=new QrCodeService().generateQrCode("cropdeal-test-token");assertTrue(png.length>100);assertEquals((byte)0x89,png[0]);assertEquals((byte)0x50,png[1]);assertEquals((byte)0x4E,png[2]);assertEquals((byte)0x47,png[3]);}
}
