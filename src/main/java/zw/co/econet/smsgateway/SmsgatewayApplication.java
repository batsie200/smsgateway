package zw.co.econet.smsgateway;

import com.cloudhopper.smpp.util.DeliveryReceipt;
import com.cloudhopper.smpp.util.DeliveryReceiptException;
import org.joda.time.DateTimeZone;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import zw.co.econet.smsgateway.smpp.SmppService;

@EnableCaching
@EnableRabbit
@SpringBootApplication
public class SmsgatewayApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(SmsgatewayApplication.class, args);
        SmppService smppService = ctx.getBean(SmppService.class);
        System.out.println(">>> Submitted sms "+smppService.sendSms("0774222193", "12345", "MySQL is an open source database management software that helps users store, organize, and later retrieve data. It has a variety of options to grant specific users nuanced permissions within the tables and databases—this tutorial will give a short overview of a few of the many options.In Part 1 of the MySQL Tutorial, we did all of the editing in MySQL as the root user, with full access to all of the databases. However, in the cases where more restrictions may be required, there are ways to create users with custom permissions."));
      try {
            DeliveryReceipt dlr = DeliveryReceipt.parseShortMessage("id:0 sub:001 dlvrd:001 submit date:1704120951 done date:1704120951 stat:DELIVRD err:000 Text:test messages", DateTimeZone.getDefault(), false);
//           dlr.getMessageId()
            System.out.println(dlr.getMessageId() != null);
        } catch (DeliveryReceiptException e) {
            e.printStackTrace();
        }

    }
}
