import com.ibm.icu.text.Normalizer;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalTest {

    public static final String MONTHS_IT = "(gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre)";
    public static final String MONTHS_FR = "(janvier|fevrier|mars|avril|mai|juin|juillet|aout|septembre|octobre|novembre|decembre)";
    public static final String MONTHS_DE = "(januar|februar|marz|april|mai|juni|juli|august|september|oktober|november|dezember)";
    public static final String MOIS_EN = "(jan|feb|mar|apr|may|jun|jul|aug|sept|oct|nov|dec)";
    public static final String JOURS_FR = "(lundi|mardi|mercredi|jeudi|vendredi|samedi|dimanche)";

    public static Matcher matcher;
    public static String str;

    public static void main(String[] args) {
        Calendar cToday = Calendar.getInstance();
        cToday.set(Calendar.HOUR, 0);
        cToday.set(Calendar.MINUTE, 0);
        cToday.set(Calendar.SECOND, 0);
        System.out.println(cToday.getTime());
        System.out.println(Calendar.getInstance().getTime());
        System.out.println("");

        str = "kommt bis Ende Juni";
        str = deleteAccent(str).toLowerCase();
        System.out.println(str);
        matcher = Pattern.compile("(\\w+)\\s*" + MONTHS_DE).matcher(str);
        if (matcher.find()) {
            if (str.contains("mitte")) str = "15 " + matcher.group(2);
            else if (str.contains("ende") && !"februar".equals(matcher.group(2))) str = "30 " + matcher.group(2);
            else if (str.contains("ende")) str = "28 " + matcher.group(2);
            else str = "01 " + matcher.group(2);
            System.out.println(str);
            str = cleanDateDE(str);
            LocalDate deliveryDate = parseDeliveryDateDE(str, "dd MM");
            if (deliveryDate != null) {
                int monthsLocal = Integer.parseInt("" + LocalDate.now().getMonthOfYear());
                System.out.println(monthsLocal);
                int monthsDelivery = Integer.parseInt("" + deliveryDate.getMonthOfYear());
                System.out.println(monthsDelivery);
                int delivery = 0;
                if (monthsLocal <= monthsDelivery) {
                    System.out.println(monthsLocal + "<" + monthsDelivery);
                    str = str + " " + Integer.parseInt("" + LocalDate.now().getYear());
                    System.out.println(str);
                } else {
                    System.out.println(monthsLocal + ">" + monthsDelivery);
                    str = str + " " + (Integer.parseInt("" + LocalDate.now().getYear()) + 1);
                    System.out.println(str);
                }

                deliveryDate = parseDeliveryDateDE(str, "dd MM yyyy");
                delivery = Days.daysBetween(LocalDate.now(), deliveryDate).getDays();
                System.out.println(delivery);
            }
        }

        System.out.println(LocalDate.now().minusDays(1).toDate());
        // System.out.println(LocalDateTime.now().minusDays(0).withHourOfDay(14).withMinuteOfHour(10).toDate());
        System.out.println();

        System.out.println(new Date(1490911200));

        str = "départ lundi";
        str = deleteAccent(str).toLowerCase();
        System.out.println(str);
        matcher = Pattern.compile("(\\w+)\\s*" + JOURS_FR).matcher(str);
        if (matcher.find()) {
            int now = LocalDate.now().dayOfWeek().get();
            System.out.println("Now day : " + now);
            int deliveryDay = getJoursFr(matcher.group(2));
            System.out.println("Delivery day : " + deliveryDay);
            if (now > deliveryDay) deliveryDay += 7;
            System.out.println("Delivery : " + (deliveryDay - now));
        }
        System.out.println();

        System.out.println(RandomStringUtils.randomAlphanumeric(14));
        System.out.println();

        int delivery = Days.daysBetween(DateTime.now(), new DateTime(Long.parseLong("1484204400000"))).getDays() + 1;
        System.out.println(delivery);

        // LocalDateTime dateT = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong("1484204400000")), ZoneId.systemDefault());
        // System.out.println(dateT);

        String content = "\"questionAnswers\":{\"6OQ2OQ67KY4J\":{\"questionDetails\":[{\"questionId\":\"2612319\",\"questionSummary\":\"Does \"chuc\"\",\"questionSummary\":\"Does \"chuc";
        Matcher matcher = Pattern.compile("([a-zA-Z]\\s*(\")\\s*[a-zA-Z])|([a-zA-Z]\\s*(\")\")").matcher(content);
        while (matcher.find()) {
            System.out.println("found");
            System.out.println(matcher.group());
        }

        System.out.println("");
        System.out.println("Modulo");
        System.out.println(62317 % 10);
        System.out.println(64378 % 5);

        System.out.println();

        float r = 1011 / 100;
        System.out.println(r);

        System.out.println("" + ((230 + (230 * 20 / 100))));
        System.out.println("" + ((12.99 + (12.99 * 20 / 100))));
        System.out.println("" + ((1.63 + (1.63 * 20 / 100))));
        System.out.println("" + ((17.54 - 1.956)));
        System.out.println("" + ((12.99 + (12.99 * 20 / 100)) + (1.63 + (1.63 * 20 / 100))));
        System.out.println("" + ((12.99 + 1.63) + ((12.99 + 1.63) * 20 / 100)));

        System.out.println();
        // int day = LocalDate.now().getDayOfMonth();
        // int month = LocalDate.now().getMonthOfYear();
        // int year = LocalDate.now().getYear();
        // int hour = LocalDateTime.now().getHour();
        // int minute = LocalDateTime.now().getMinute();
        // int seconde = LocalDateTime.now().getSecond();
        // int nano = LocalDateTime.now().getNano() / 100000;
        // long millis = System.currentTimeMillis() % 1000;
        //
        // System.out.println(year + "" + day + "" + month + "" + hour + "" + minute + "" + seconde + "_" + getNumberRandom(3) + "." + getNumberRandom(14));
        //
        // System.out.println(Math.random() * 10);

        LocalDateTime now = LocalDateTime.now();
        System.out.println(Math.random() * 1e3 + 1);
        String date = now.getYear() + "" + now.getDayOfMonth() + "" + now.getDayOfWeek() + "" + now.getHourOfDay() + "" + now.getMinuteOfHour();
        String cookie = "B2W-SID=" + (Math.random() * 1e3 + 1) + date + "" + now.getMillisOfSecond() + "; ";
        System.out.println(cookie);

        System.out.println();
        str = "https://www.gommadiretto.it/cgi-bin/rshop.pl?dsco=130&Breite=165&Quer=70&Felge=14&Speed=&kategorie=&Marke=&ranzahl=4&tyre_for=&x_tyre_for=&sort_by=groesse&rsmFahrzeugart=ALL&search_tool=standard&Label=E-B-72-3&details=Ordern&typ=R-118111";
        String firstPath = StringUtils.substringBefore(str, "?");
        String endPath = StringUtils.substringAfter(str, "details=");
        String productPathFinal = firstPath + "?details=" + endPath;
        System.out.println(productPathFinal);

        System.out.println();
        str = "https://www.clubtek.pt/index.php?route=product/category&path=407_414_423&page=2";
        System.out.println("" + str);
        Matcher mUniqueId = Pattern.compile("path=([0-9_]+)").matcher(str);
        if (mUniqueId.find()) System.out.println(mUniqueId.group(1));

        codeAscii(" ");
        // ScriptEngineManager manager = new ScriptEngineManager();
        // ScriptEngine moteur = manager.getEngineByName("js");
        // // try {
        // //// moteur.eval("for (var characterMap31 = [], characterSet31 = \"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz\", characterMap47 = [],
        // characterSet47 = \"!\\\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\", i = 0; i < characterSet31.length; i++)
        // characterMap31[characterSet31.charAt(i)] = characterSet31.charAt((i + 31) % 62);for (var j = 0; j < characterSet47.length; j++)
        // characterMap47[characterSet47.charAt(j)] = characterSet47.charAt((j + 47) % 94);function decode47(text) {var toReturn = \"\";for (i = 0; i < text.length; i++)
        // {var currentChar = text.charAt(i);toReturn += currentChar >= \"!\" && currentChar <= \"~\" ? characterMap47[currentChar] : currentChar}return toReturn}var result
        // = decode47('%#x)t$ #2DA36CUD9JjCJ !: |:\<C@ q=F6UD9JjE@@E9 a]_ s@?8=6');");
        // // } catch (ScriptException e) {
        // // e.printStackTrace();
        // // }

    }

    private static String getNumberRandom(int n) {
        String r = "";
        for (int i = 0; i < n; i++) {
            r += (int) (Math.random() * 10);
        }
        return r;
    }

    public static int getJoursFr(String jour) {
        if (jour.equals("lundi")) return 1;
        if (jour.equals("mardi")) return 2;
        if (jour.equals("mercredi")) return 3;
        if (jour.equals("jeudi")) return 4;
        if (jour.equals("vendredi")) return 5;
        if (jour.equals("samedi")) return 6;
        if (jour.equals("dimanche")) return 7;
        return 0;
    }

    public static String cleanDate(String strDelivery) {
        if (strDelivery.contains("janvier")) strDelivery = strDelivery.replace("janvier", "01");
        if (strDelivery.contains("fevrier")) strDelivery = strDelivery.replace("fevrier", "02");
        if (strDelivery.contains("mars")) strDelivery = strDelivery.replace("mars", "03");
        if (strDelivery.contains("avril")) strDelivery = strDelivery.replace("avril", "04");
        if (strDelivery.contains("mai")) strDelivery = strDelivery.replace("mai", "05");
        if (strDelivery.contains("juin")) strDelivery = strDelivery.replace("juin", "06");
        if (strDelivery.contains("juillet")) strDelivery = strDelivery.replace("juillet", "07");
        if (strDelivery.contains("aout")) strDelivery = strDelivery.replace("aout", "08");
        if (strDelivery.contains("septembre")) strDelivery = strDelivery.replace("septembre", "09");
        if (strDelivery.contains("octobre")) strDelivery = strDelivery.replace("octobre", "10");
        if (strDelivery.contains("novembre")) strDelivery = strDelivery.replace("novembre", "11");
        if (strDelivery.contains("decembre")) strDelivery = strDelivery.replace("decembre", "12");
        return strDelivery;
    }

    static LocalDate parseDeliveryDate(final String deliveryDateText) {
        DateTimeFormatter DELIVERY_DATE_PARSER = DateTimeFormat.forPattern("dd MM yyyy").withLocale(Locale.getDefault());
        try {
            return LocalDate.parse(deliveryDateText, DELIVERY_DATE_PARSER);
        } catch (Exception exc) {
            System.out.println("Delivery date not parseable [" + deliveryDateText + "] - Exc : " + exc);
        }
        return null;
    }

    private static String cleanDateDE(String strDelivery) {
        if (strDelivery.contains("januar")) strDelivery = strDelivery.replace("januar", "01");
        if (strDelivery.contains("februar")) strDelivery = strDelivery.replace("februar", "02");
        if (strDelivery.contains("marz")) strDelivery = strDelivery.replace("marz", "03");
        if (strDelivery.contains("april")) strDelivery = strDelivery.replace("april", "04");
        if (strDelivery.contains("mai")) strDelivery = strDelivery.replace("mai", "05");
        if (strDelivery.contains("juni")) strDelivery = strDelivery.replace("juni", "06");
        if (strDelivery.contains("juli")) strDelivery = strDelivery.replace("juli", "07");
        if (strDelivery.contains("august")) strDelivery = strDelivery.replace("august", "08");
        if (strDelivery.contains("september")) strDelivery = strDelivery.replace("september", "09");
        if (strDelivery.contains("oktober")) strDelivery = strDelivery.replace("oktober", "10");
        if (strDelivery.contains("november")) strDelivery = strDelivery.replace("november", "11");
        if (strDelivery.contains("dezember")) strDelivery = strDelivery.replace("dezember", "12");
        return strDelivery;
    }

    private static LocalDate parseDeliveryDateDE(final String deliveryDateText, String pattern) {
        DateTimeFormatter DELIVERY_DATE_PARSER = DateTimeFormat.forPattern(pattern).withLocale(Locale.getDefault());
        try {
            return LocalDate.parse(deliveryDateText, DELIVERY_DATE_PARSER);
        } catch (Exception exc) {
            System.out.println("Delivery date not parseable [" + deliveryDateText + "] - Exc : " + exc);
        }
        return null;
    }

    private static String getNumberOnString(String text) {
        return text.replaceAll("[^\\d+]", "");
    }

    private static String deleteAccent(String strTxt) {
        String strTmpTxt = strTxt;
        strTmpTxt = Normalizer.normalize(strTmpTxt, Normalizer.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return strTmpTxt;
    }

    private static void codeAscii(String text) {
        System.out.println(text);
        for (int idx = 0; idx < text.length(); idx++) {
            System.out.println("" + text.charAt(idx) + " => " + (int) text.charAt(idx));
        }
    }

    private static String encode(String text) {
        for (int iChar = 128; iChar < 192; iChar++) {
            text = text.replace("" + (char) 195 + (char) iChar, "" + (char) (iChar + 64));
        }
        return text;
    }

    private static String replaceCarac(String text, int carac, String replacement) {
        return text.replace("" + (char) carac, replacement);
    }
}
