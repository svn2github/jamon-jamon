package weather;

public class WeatherReport
{
    public WeatherReport(String p_day, String p_city)
    {
        m_day = p_day;
        m_city = p_city;
    }

    public String getCity() { return m_city; }
    public String getDay() { return m_day; }

    private final String m_day;
    private final String m_city;
}
