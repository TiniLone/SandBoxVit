import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeliveryParser {
    private static final String MOIS_EN_SM = "(jan|feb|mar|apr|may|jun|jul|aug|sept|oct|nov|dec)";
    private static final String MOIS_EN = "(january|february|march|april|may|june|july|august|september|october|november|december)";

    public static void main(String[] args) {
        String dateTextRaw = "Order Now And Get This From Early Oct 2017";
        System.out.println(dateTextRaw);
        dateTextRaw = StringUtils.lowerCase(dateTextRaw);
        dateTextRaw = StringUtils.replace(dateTextRaw, "from early", "01");
        dateTextRaw = StringUtils.replace(dateTextRaw, "from mid", "15");
        dateTextRaw = StringUtils.replace(dateTextRaw, "from late", "30");
        System.out.println(dateTextRaw);

        DateTime deliveryDateTime = parseDateDelivery(dateTextRaw, "\\d+\\s*" + MOIS_EN_SM + "\\s*\\d{4}", "dd MMM yyyy");
        int delivery = Days.daysBetween(DateTime.now(), deliveryDateTime).getDays();
        System.out.println(delivery);

        dateTextRaw = "Order Now And Get This From Tuesday 17th October";
        dateTextRaw = StringUtils.lowerCase(dateTextRaw);
        Matcher deliveryMatcher = Pattern.compile("\\d+(\\w+)*\\s*" + MOIS_EN + "(\\d{4})*").matcher(dateTextRaw);
        if (deliveryMatcher.find()) {
            String dateText = deliveryMatcher.group();
            String dateExtension = deliveryMatcher.group(1);
            System.out.println(dateExtension);
            if (StringUtils.isNotBlank(dateExtension)) {
                dateText = StringUtils.replace(dateText, dateExtension, "");
            }
            String yearText = deliveryMatcher.group(3);
            System.out.println(yearText);
            if (StringUtils.isBlank(yearText)) {
                dateText += " 2017";
                System.out.println(dateText);
            }
            deliveryDateTime = parseDateDelivery(dateText, "dd MMMM yyyy");
            System.out.println(deliveryDateTime);
            delivery = Days.daysBetween(DateTime.now(), deliveryDateTime).getDays();
            System.out.println(delivery);
        }
    }

    private static DateTime parseDateDelivery(String deliveryText, String patternDeliveryDate, String patternDateFormat) {
        Matcher matcher = Pattern.compile(patternDeliveryDate).matcher(deliveryText);
        if (matcher.find()) {
            String dateText = matcher.group();
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(patternDateFormat).withLocale(Locale.UK);
            try {
                return DateTime.parse(dateText, dateTimeFormatter);
            } catch (Exception exc) {
                System.out.println("Delivery date not parseable [" + dateText + "] - Exc : " + exc);
            }
        }
        return null;
    }

    private static DateTime parseDateDelivery(String deliveryText, String patternDateFormat) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(patternDateFormat).withLocale(Locale.UK);
        try {
            return DateTime.parse(deliveryText, dateTimeFormatter);
        } catch (Exception exc) {
            System.out.println("Delivery date not parseable [" + deliveryText + "] - Exc : " + exc);
        }
        return null;
    }
}
