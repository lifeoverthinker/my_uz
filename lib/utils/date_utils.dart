/// Date helpers used across the app to avoid duplication.
DateTime mondayOfWeek(DateTime d) {
  final wd = d.weekday; // 1=Mon
  return DateTime(d.year, d.month, d.day - (wd - 1));
}

DateTime stripTime(DateTime d) => DateTime(d.year, d.month, d.day);

bool isSameDay(DateTime a, DateTime b) => a.year == b.year && a.month == b.month && a.day == b.day;

String weekdayShort(int weekday) {
  const days = ['P','W','Ś','C','P','S','N'];
  return days[weekday - 1];
}

String formatMonthShort(int m) {
  const mies = ['sty','lut','mar','kwi','maj','cze','lip','sie','wrz','paź','lis','gru'];
  return mies[m - 1];
}