import java.time.Instant;
import java.time.ZoneOffset;

class NDArrayMessage implements Serializable {
    /**
     * Get the current time in utc in milliseconds
     * @return the current time in utc in
     * milliseconds
     */
    public static long getCurrentTimeUtc() {
	Instant instant = Instant.now();
	ZonedDateTime dateTime = instant.atZone(ZoneOffset.UTC);
	return dateTime.toInstant().toEpochMilli();
    }

}

