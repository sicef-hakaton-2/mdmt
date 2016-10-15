package rs.mdmt.hakaton;

/**
 * Created by Tomasevic on 15.11.2015.
 */
public class Departure
{
    String source, destination, time;
    int seats;

    public Departure()
    {
        source = "";
        destination = "";
        time = "";
        seats = 0;
    }
    public Departure(String s, String d, String t, int se)
    {
        source = s;
        destination = d;
        time = t;
        seats = se;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getDestination()
    {
        return destination;
    }

    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    public int getSeats()
    {
        return seats;
    }

    public void setSeats(int seats)
    {
        this.seats = seats;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }
}
