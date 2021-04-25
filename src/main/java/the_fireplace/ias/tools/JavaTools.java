package the_fireplace.ias.tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class JavaTools {
	public static int[] getDate() {
		int[] ret = new int[3];
		ret[0]=LocalDateTime.now().getMonthValue();
		ret[1]=LocalDateTime.now().getDayOfMonth();
		ret[2]=LocalDateTime.now().getYear();
		return ret;
	}

	public static String getFormattedDate() {
		DateTimeFormatter format = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
		LocalDate date = LocalDateTime.now().withDayOfMonth(getDate()[1]).withMonth(getDate()[0]).withYear(getDate()[2]).toLocalDate();
		return date.format(format);
	}
}
